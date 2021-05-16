package pro.belbix.ethparser.codegen;

import pro.belbix.ethparser.dto.v0.ContractSourceCodeDTO;
import pro.belbix.ethparser.service.AbiProviderService.SourceCodeResult;

public class ContractSourceModelConverter {

  public static ContractSourceCodeDTO toDTO(SourceCodeResult result){
    ContractSourceCodeDTO csdto = new ContractSourceCodeDTO();
    csdto.setSourceCode(result.getSourceCode());
    csdto.setAbi(result.getAbi());
    csdto.setContractName(result.getContractName());
    csdto.setCompilerVersion(result.getCompilerVersion());
    csdto.setOptimizationUsed(Boolean.parseBoolean(result.getOptimizationUsed()));
    csdto.setRuns(result.getRuns());
    csdto.setConstructorArguments(result.getConstructorArguments());
    csdto.setEVMVersion(result.getEVMVersion());
    csdto.setLibrary(result.getLibrary());
    csdto.setLicenseType(result.getLicenseType());
    csdto.setProxy(Boolean.parseBoolean(result.getProxy()));
    csdto.setImplementation(result.getImplementation());
    csdto.setSwarmSource(result.getSwarmSource());
    return csdto;
  }

  public static SourceCodeResult toSourceCodeResult(ContractSourceCodeDTO csdto){
    SourceCodeResult result = new SourceCodeResult();
    result.setSourceCode(csdto.getSourceCode());
    result.setAbi(csdto.getAbi());
    result.setContractName(csdto.getContractName());
    result.setCompilerVersion(csdto.getCompilerVersion());
    result.setOptimizationUsed(String.valueOf(csdto.getOptimizationUsed()));
    result.setRuns(csdto.getRuns());
    result.setConstructorArguments(csdto.getConstructorArguments());
    result.setEVMVersion(csdto.getEVMVersion());
    result.setLibrary(csdto.getLibrary());
    result.setLicenseType(csdto.getLicenseType());
    result.setProxy(String.valueOf(csdto.getProxy()));
    result.setImplementation(csdto.getImplementation());
    result.setSwarmSource(csdto.getSwarmSource());
    return result;
  }


}
