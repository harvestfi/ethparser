package pro.belbix.ethparser.web3.harvest.db;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import pro.belbix.ethparser.Application;
import pro.belbix.ethparser.dto.HarvestDTO;
import pro.belbix.ethparser.repositories.HarvestRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class)
@ActiveProfiles("test")
public class HarvestDBServiceTest {

    @Autowired
    private HarvestDBService harvestDBService;
    @Autowired
    private HarvestRepository harvestRepository;

    @Test
    @Ignore("DB data required")
    // todo create a normal test
    public void testFillProfit() {
    
    	/* testing for all cases listed here ->
    	if (!"Withdraw".equals(dto.getMethodName())
            || dto.getOwnerBalance() == null
            || dto.getOwnerBalance() != 0.0
            || dto.getAmount() == null
            || dto.getAmount() == 0.0
            || "PS".equals(dto.getVault())
            || "PS_V0".equals(dto.getVault())) {
            return;
        }
	*/
	
        List<HarvestDTO> transfers = new ArrayList<>();
        transfers.add(createDto(transfers.size(), "Withdraw", 100, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
        transfers.add(createDto(transfers.size(), "Deposit", null, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
        transfers.add(createDto(transfers.size(), "Deposit", 0.0, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
        transfers.add(createDto(transfers.size(), "Deposit", 100, null, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
        transfers.add(createDto(transfers.size(), "Deposit", 100, 0.0, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
        transfers.add(createDto(transfers.size(), "Deposit", 100, 100, "PS", 41.2912731552351, 0));
        transfers.add(createDto(transfers.size(), "Deposit", 100, 100, "PS_V0", 41.2912731552351, 0));
        
        for (HarvestDTO transfer : transfers){
        
                // profit should remain unchanged -> delta is 0.0
                // do not have to worry about the fetchLatestSinceLastWithdraw call because control should not reach that point
        	HarvestDBService.fillProfit(transfer);
    		assertEquals("fillProfit", "41,2912731552351", String.format("%.13f", transfer.getProfit()));
        	
        }
        
        /* next portion utlizes DB call inherently in the fetchLatestSinceLastWithdraw function so not sure how to test without the fetch call  ->
    	// find relevant transfers (from last full withdraw)
        List<HarvestDTO> transfers = harvestRepository.fetchLatestSinceLastWithdraw(
            dto.getOwner(),
            dto.getVault(),
            dto.getBlockDate());

        // for new transaction DB can still not write the object at this moment
        if (transfers.stream().noneMatch(h -> h.getId().equalsIgnoreCase(dto.getId()))) {
            transfers.add(dto);
        }
	*/
	
	//Testing fillProfits with legitimate values
	
	HarvestDTO testFillProfit = createDto(0, "Withdraw", 0, 100, "UNI_LP_USDC_FARM", 0, 0));
	HarvestDBService.fillProfit(testFillProfit);
	assertEquals("fillProfit", "100,000000", String.format("%.6f", testFillProfit.getProfit()));
	  
    }
    
    public void testCalculateProfit(){
    
    /*  testing for all cases listed here ->
    	if ((!"Withdraw".equals(transfer.getMethodName())
                && !"Deposit".equals(transfer.getMethodName()))
                || transfer.getAmount() == null
                || transfer.getAmount() == 0.0
            ) {
                continue;
            }
     */
     
    double profit = 0; 
     
    List<HarvestDTO> transfers = new ArrayList<>();
    transfers.add(createDto(transfers.size(), "Staked", 100, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
    transfers.add(createDto(transfers.size(), "Deposit", null, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
    transfers.add(createDto(transfers.size(), "Deposit", 0.0, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0));
    
    //According to calculateProfit, this should return 0
    profit = HarvestDBService.calculateProfit(transfers);
    assertEquals("profit", "0,000000", String.format("%.6f", profit));
    
    /* testing for all cases listed here ->
    	double sharePrice = transfer.getSharePrice();
            if (transfer.getSharePrice() == null
                || transfer.getSharePrice() == 0.0) {
                sharePrice = 1.0;
            }
     */

    //According to calculateProfit, this should return 100
    transfers.add(createDto(transfers.size(), "Withdraw", 0, 100, "UNI_LP_USDC_FARM", 41.2912731552351, 0.0));
    profit = HarvestDBService.calculateProfit(transfers);
    assertEquals("profit", "100,000000", String.format("%.6f", profit));
    
    //According to calculateProfit, this should now return 200
    transfers.add(createDto(transfers.size(), "Withdraw", 0, 100, "UNI_LP_USDC_FARM", 41.2912731552351, null));
    profit = HarvestDBService.calculateProfit(transfers);
    assertEquals("profit", "200,000000", String.format("%.6f", profit));
    
    //rest of calculateProfit -> legitimate values
    
    //According to calculateProfit, this should now return 100
    transfers.add(createDto(transfers.size(), "Deposit", 0, 25, "UNI_LP_USDC_FARM", 41.2912731552351, 4));
    profit = HarvestDBService.calculateProfit(transfers);
    assertEquals("profit", "100,000000", String.format("%.6f", profit));
    
    
    }
    

    private HarvestDTO createDto(int id, String methodName, double ownerBalance, double amount, String vault, double profit, double sharePrice) {
        HarvestDTO dto = new HarvestDTO();
        dto.setId(id + "");
        dto.setMethodName(methodName);
        dto.setOwnerBalance(ownerBalance);
        dto.setAmount(amount);
        dto.setVault(vault);
        dto.setProfit(profit);
        dto.setSharePrice(sharePrice);
        return dto;
    }
}
