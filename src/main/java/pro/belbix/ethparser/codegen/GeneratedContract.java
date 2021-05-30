package pro.belbix.ethparser.codegen;

import java.util.Collection;
import java.util.Map;
import lombok.extern.log4j.Log4j2;
import org.web3j.abi.datatypes.Event;
import org.web3j.abi.datatypes.Function;

@Log4j2
public class GeneratedContract {

  private final String name;
  private final String address;
  private final Map<String, Event> eventsByHash;
  private final Map<String, String> eventHashByName;
  private final Map<String, FunctionWrapper> functionsByMethodId;
  private boolean proxy = false;


  public GeneratedContract(String name, String address,
      Map<String, Event> eventsByHash,
      Map<String, String> eventHashByName,
      Map<String, FunctionWrapper> functionsByMethodId) {
    this.name = name;
    this.address = address;
    this.eventsByHash = eventsByHash;
    this.eventHashByName = eventHashByName;
    this.functionsByMethodId = functionsByMethodId;
  }

  public boolean isProxy() {
    return proxy;
  }

  public void setProxy(boolean proxy) {
    this.proxy = proxy;
  }

  public Event getEvent(String hash) {
    return eventsByHash.get(hash);
  }

  public String getEventHashByName(String name) {
    return eventHashByName.get(name);
  }

  public FunctionWrapper getFunction(String hash) {
    return functionsByMethodId.get(hash);
  }

  public String getName() {
    return name;
  }

  public String getAddress() {
    return address;
  }

  public Collection<FunctionWrapper> getFunctions() {
    return functionsByMethodId.values();
  }

  public Collection<Event> getEvents() {
    return eventsByHash.values();
  }
}
