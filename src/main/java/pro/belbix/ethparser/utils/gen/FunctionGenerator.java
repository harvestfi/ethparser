package pro.belbix.ethparser.utils.gen;

import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.log4j.Log4j2;
import org.jetbrains.annotations.NotNull;
import org.web3j.abi.TypeReference;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.DynamicArray;
import org.web3j.abi.datatypes.Function;
import org.web3j.abi.datatypes.Type;
import org.web3j.protocol.core.methods.response.AbiDefinition;
import org.web3j.protocol.core.methods.response.AbiDefinition.NamedType;
import org.web3j.tuples.generated.Tuple3;
import org.web3j.utils.Collection;

@Log4j2
public class FunctionGenerator {

  private final HashMap<Integer, ClassName> structClassNameMap = new HashMap<>();
  private final List<NamedType> structsNamedTypeList = new ArrayList<>();
  private final List<AbiDefinition> functionDefinitions;
  private final String contract;

  public FunctionGenerator(List<AbiDefinition> functionDefinitions, String contract) {
    this.functionDefinitions = functionDefinitions;
    this.contract = contract;
    initStructTypes();
    buildStructsNamedTypesList();
  }

  public Map<AbiDefinition, Tuple3<String, String, Object[]>> generate() {
    Map<AbiDefinition, Tuple3<String, String, Object[]>> result = new HashMap<>();
    for (AbiDefinition abiDefinition : functionDefinitions) {
      result.put(abiDefinition, buildFunctions(abiDefinition));
    }
    return result;
  }

  private Tuple3<String, String, Object[]> buildFunctions(AbiDefinition functionDefinition) {
    try {
      String inputParameterTypes = Collection.join(
          buildParameterTypes(functionDefinition.getInputs()),
          ", \n",
          this::createMappedParameterTypes);
      List<TypeName> outputParameterTypes =
          buildTypeNames(functionDefinition.getOutputs());
      return buildFunction(functionDefinition.getName(), inputParameterTypes, outputParameterTypes);
    } catch (Exception e) {
      log.error("Error build functions", e);
      throw new RuntimeException(e);
    }
  }

  private List<TypeName> buildTypeNames(
      List<AbiDefinition.NamedType> namedTypes) throws ClassNotFoundException {

    List<TypeName> result = new ArrayList<>(namedTypes.size());
    for (AbiDefinition.NamedType namedType : namedTypes) {
      if (namedType.getType().equals("tuple")) {
        result.add(structClassNameMap.get(namedType.structIdentifier()));
      } else if (namedType.getType().startsWith("tuple")
          && namedType.getType().contains("[")) {
        result.add(buildStructArrayTypeName(namedType));
      } else {
        result.add(buildTypeName(namedType.getType()));
      }
    }
    return result;
  }

  static TypeName buildTypeName(String typeDeclaration) throws ClassNotFoundException {

    final String solidityType = trimStorageDeclaration(typeDeclaration);

    final TypeReference typeReference =
        TypeReference.makeTypeReference(solidityType, false, false);

    return TypeName.get(typeReference.getType());
  }

  private static String trimStorageDeclaration(String type) {
    if (type.endsWith(" storage") || type.endsWith(" memory")) {
      return type.split(" ")[0];
    } else {
      return type;
    }
  }

  private TypeName buildStructArrayTypeName(NamedType namedType) {
    String structName;
    if (namedType.getInternalType().isEmpty()) {
      structName =
          structClassNameMap
              .get(
                  structsNamedTypeList.stream()
                      .filter(struct -> isSameStruct(namedType, struct))
                      .collect(Collectors.toList())
                      .get(0)
                      .structIdentifier())
              .simpleName();

    } else {
      structName =
          namedType
              .getInternalType()
              .substring(
                  namedType.getInternalType().lastIndexOf(".") + 1,
                  namedType.getInternalType().indexOf("["));
    }

    String arrayLength =
        namedType
            .getType()
            .substring(
                namedType.getType().indexOf('[') + 1,
                namedType.getType().indexOf(']'));
    if (!arrayLength.isEmpty() && Integer.parseInt(arrayLength) > 0) {
      return ParameterizedTypeName.get(
          ClassName.get("org.web3j.abi.datatypes.generated", "StaticArray" + arrayLength),
          ClassName.get("", structName));
    } else {
      return ParameterizedTypeName.get(
          ClassName.get(DynamicArray.class), ClassName.get("", structName));
    }
  }

  private boolean isSameStruct(NamedType base, NamedType target) {
    for (NamedType baseField : base.getComponents()) {
      if (!target.getComponents().stream()
          .anyMatch(
              targetField ->
                  baseField.getType().equals(targetField.getType())
                      && baseField.getName().equals(targetField.getName()))) {
        return false;
      }
    }
    return true;
  }

  private Tuple3<String, String, Object[]> buildFunction(
      String functionName,
      String inputParameters,
      List<TypeName> outputParameterTypes) {

    List<Object> objects = new ArrayList<>();
    objects.add(Function.class);
    objects.add(functionName);

    objects.add(Arrays.class);
    objects.add(Type.class);
    objects.add(inputParameters);

    objects.add(Arrays.class);
    objects.add(TypeReference.class);
    for (TypeName outputParameterType : outputParameterTypes) {
      objects.add(TypeReference.class);
      objects.add(outputParameterType);
    }

    String asListParams =
        Collection.join(outputParameterTypes, ", ", typeName -> "new $T<$T>() {}");

    return new Tuple3<>(
        contract,
        "new $T($N, \n$T.<$T>asList($L), \n$T"
            + ".<$T<?>>asList("
            + asListParams
            + "))",
        objects.toArray());
  }

  private void initStructTypes() {
    final List<AbiDefinition.NamedType> orderedKeys = extractStructs(functionDefinitions);
    int structCounter = 0;
    for (AbiDefinition.NamedType namedType : orderedKeys) {
      String internalType = namedType.getInternalType();
      String structName;
      if (internalType == null || internalType.isEmpty()) {
        structName = "Struct" + structCounter;
      } else {
        structName = internalType.substring(internalType.lastIndexOf(".") + 1);
      }
      structClassNameMap.put(namedType.structIdentifier(), ClassName.get("", structName));
    }
  }

  @NotNull
  private List<AbiDefinition.NamedType> extractStructs(
      final List<AbiDefinition> functionDefinitions) {
    final HashMap<Integer, AbiDefinition.NamedType> structMap = new LinkedHashMap<>();
    functionDefinitions.stream()
        .flatMap(
            definition -> {
              List<AbiDefinition.NamedType> parameters = new ArrayList<>();
              parameters.addAll(definition.getInputs());
              parameters.addAll(definition.getOutputs());
              return parameters.stream()
                  .filter(namedType -> namedType.getType().equals("tuple"));
            })
        .forEach(
            namedType -> {
              structMap.put(namedType.structIdentifier(), namedType);
              extractNested(namedType).stream()
                  .filter(
                      nestedNamedStruct ->
                          nestedNamedStruct.getType().equals("tuple"))
                  .forEach(
                      nestedNamedType ->
                          structMap.put(
                              nestedNamedType.structIdentifier(),
                              nestedNamedType));
            });

    return structMap.values().stream()
        .sorted(Comparator.comparingInt(AbiDefinition.NamedType::nestedness))
        .collect(Collectors.toList());
  }

  private java.util.Collection<? extends AbiDefinition.NamedType> extractNested(
      final AbiDefinition.NamedType namedType) {
    if (namedType.getComponents().size() == 0) {
      return new ArrayList<>();
    } else {
      List<AbiDefinition.NamedType> nestedStructs = new ArrayList<>();
      namedType
          .getComponents()
          .forEach(
              nestedNamedStruct -> {
                nestedStructs.add(nestedNamedStruct);
                nestedStructs.addAll(extractNested(nestedNamedStruct));
              });
      return nestedStructs;
    }
  }

  private void buildStructsNamedTypesList() {
    structsNamedTypeList.addAll(
        functionDefinitions.stream()
            .flatMap(
                definition -> {
                  List<AbiDefinition.NamedType> parameters = new ArrayList<>();
                  parameters.addAll(definition.getInputs());
                  parameters.addAll(definition.getOutputs());
                  return parameters.stream()
                      .filter(
                          namedType ->
                              namedType.getType().equals("tuple"));
                })
            .collect(Collectors.toList()));
  }

  String createInputParameters(List<AbiDefinition.NamedType> namedTypes)
      throws ClassNotFoundException {

    return Collection.join(
        buildParameterTypes(namedTypes),
        ", \n",
        this::createMappedParameterTypes);

  }

  private List<ParameterSpec> buildParameterTypes(List<AbiDefinition.NamedType> namedTypes)
      throws ClassNotFoundException {

    List<ParameterSpec> result = new ArrayList<>(namedTypes.size());
    for (int i = 0; i < namedTypes.size(); i++) {
      AbiDefinition.NamedType namedType = namedTypes.get(i);

      String name = createValidParamName(namedType.getName(), i);
      String type = namedTypes.get(i).getType();

      if (type.equals("tuple")) {
        result.add(
            ParameterSpec.builder(
                structClassNameMap.get(namedType.structIdentifier()), name)
                .build());
      } else if (type.startsWith("tuple") && type.contains("[")) {
        result.add(
            ParameterSpec.builder(buildStructArrayTypeName(namedType), name)
                .build());
      } else {
        result.add(ParameterSpec.builder(buildTypeName(type), name).build());
      }
    }
    return result;
  }

  static String createValidParamName(String name, int idx) {
    if (name == null || name.equals("")) {
      return "param" + idx;
    } else {
      return name;
    }
  }

  String createMappedParameterTypes(ParameterSpec parameterSpec) {
    if (parameterSpec.type instanceof ParameterizedTypeName) {
      List<TypeName> typeNames = ((ParameterizedTypeName) parameterSpec.type).typeArguments;
      if (typeNames.size() != 1) {
        throw new UnsupportedOperationException(
            "Only a single parameterized type is supported");
      } else if (structClassNameMap.values().stream()
          .map(ClassName::simpleName)
          .anyMatch(
              name ->
                  name.equals(
                      ((ClassName)
                          ((ParameterizedTypeName)
                              parameterSpec.type)
                              .typeArguments.get(0))
                          .simpleName()))) {
        String structName =
            structClassNameMap.values().stream()
                .map(ClassName::simpleName)
                .filter(
                    name ->
                        name.equals(
                            ((ClassName)
                                ((ParameterizedTypeName)
                                    parameterSpec
                                        .type)
                                    .typeArguments.get(
                                    0))
                                .simpleName()))
                .collect(Collectors.toList())
                .get(0);
        return "new "
            + parameterSpec.type
            + "("
            + structName
            + ".class, "
            + parameterSpec.name
            + ")";
      } else {
        String parameterSpecType = parameterSpec.type.toString();
        TypeName typeName = typeNames.get(0);
        String typeMapInput = typeName + ".class";
        String componentType = typeName.toString();
        if (typeName instanceof ParameterizedTypeName) {
          List<TypeName> typeArguments = ((ParameterizedTypeName) typeName).typeArguments;
          if (typeArguments.size() != 1) {
            throw new UnsupportedOperationException(
                "Only a single parameterized type is supported");
          }
          TypeName innerTypeName = typeArguments.get(0);
          componentType = ((ParameterizedTypeName) typeName).rawType.toString();
          parameterSpecType =
              ((ParameterizedTypeName) parameterSpec.type).rawType
                  + "<"
                  + componentType
                  + ">";
          typeMapInput = componentType + ".class,\n" + innerTypeName + ".class";
        }
        return "new "
            + parameterSpecType
            + "(\n"
            + "        "
            + componentType
            + ".class,\n"
            + "        org.web3j.abi.Utils.typeMap("
            + parameterSpec.name
            + ", "
            + typeMapInput
            + "))";
      }
    } else if (structClassNameMap.values().stream()
        .map(ClassName::simpleName)
        .noneMatch(name -> name.equals(parameterSpec.type.toString()))) {
      String constructor = "new " + parameterSpec.type + "(";
      if (Address.class.getCanonicalName().equals(parameterSpec.type.toString())) {
        constructor += Address.DEFAULT_LENGTH + ", ";
      }
      return constructor + parameterSpec.name + ")";
    } else {
      return parameterSpec.name;
    }
  }

}
