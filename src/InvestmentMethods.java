import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class InvestmentMethods {

	public static void main(String[] args) {
		
		System.out.println("orders determination process started.....");
		
		Connection conn = DBConnection.getInstance();
		InvestmentMethods im = new InvestmentMethods();
		List<Holding> holdings = im.getListOfHoldingsFromDatabase(conn);
		List<Model> models = im.getListOfModelsFromDatabase(conn);
		List<Order> orders = im.determineOrder(holdings, models);
		im.addListOfOrdersToDatabase(conn, orders);
		DBConnection.close();
		
		System.out.println("orders determination process completed .....");
		
			
	
	}
	
	public List<Holding> getListOfHoldingsFromDatabase(Connection conn){
		List<Holding> holdings = new ArrayList<>();
		try{
			PreparedStatement stmt = conn.prepareStatement("select * from holding");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Holding h = new Holding();
				h.setAmt(rs.getFloat("amt"));
				h.setSec(rs.getString("sec"));
				holdings.add(h);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		return holdings;
	}
	
	public List<Model> getListOfModelsFromDatabase(Connection conn){
		List<Model> models = new ArrayList<>();
		try{
			PreparedStatement stmt = conn.prepareStatement("select * from model");
			ResultSet rs = stmt.executeQuery();
			while(rs.next()){
				Model m = new Model();
				m.setPercent(rs.getFloat("percent"));
				m.setSec(rs.getString("sec"));
				models.add(m);
			}
			
		}catch(SQLException e){
			e.printStackTrace();
		}
		return models;
	}
	
	public void addListOfOrdersToDatabase(Connection conn, List<Order> orders){
		try{
			for(Order order:orders){
				PreparedStatement stmt = conn.prepareStatement("insert into ord values(?, ?, ?)");
					
				System.out.println("SEC VALUE: " + order.getSec());
				stmt.setString(1, order.getSec());
				stmt.setString(2, String.valueOf(order.getTrans()));
				stmt.setFloat(3, order.getDiffAmount());
				stmt.execute();
				
				System.out.println("Data inserted into ORD .....");
			}
		}catch(SQLException e){
			e.printStackTrace();
		}
	}
	
	
	public List<Order> determineOrder(List<Holding> holdings, List<Model> models) {
		
		//step1: calculate total amount
		Float amount = calculateAmount(holdings); //
		
		//step2: add holdings in original list which are in model but not in original list with amount = 0
		Map<String, Float> holdingsMap = addHoldingWithAllStocks(holdings, models);
		
		//step3: add models in original list of models which are in holdings but not in original list of model with percent = 0 
		Map<String, Float> modelsMap = addModelsWithAllStocks(holdings, models);
		
		//step4: determine list of order using holdings with all stocks and models with all stocks
		List<Order> orders = determineOrderWithAllStocks(holdingsMap, modelsMap, amount);
		
		return orders;
	}
	
	private Map<String, Float> addHoldingWithAllStocks(List<Holding> holdings, List<Model> models) {
		Map<String, Float> holdingsMap = new HashMap<>();
		for(Holding holding : holdings){//add key and value from holding list into holdingsMap.
			holdingsMap.put(holding.getSec(), holding.getAmt());
		} //Then appending those securities into holdingsMap that are unavailable in holdings. 
		for(Model model : models) {
			if(!holdingsMap.containsKey(model.getSec())){
				holdingsMap.put(model.getSec(), new Float(0));
			}
		}
		return holdingsMap;
	}

	private Map<String, Float> addModelsWithAllStocks(List<Holding> holdings, List<Model> models) {
		
		Map<String, Float> modelsMap = new HashMap<>();
		for(Model model: models) {
			modelsMap.put(model.getSec(), model.getPercent());
		}
		for(Holding holding: holdings) { 
			if(!modelsMap.containsKey(holding.getSec())) {
				modelsMap.put(holding.getSec(),new Float(0));
			}
		}
		
		return modelsMap;
	}
	
	


	
	public List<Order> determineOrderWithAllStocks(Map<String, Float> holdingsMap, Map<String, Float> modelsMap, Float amount){
		List<Order> orders = new ArrayList<Order>();
		for(Entry<String, Float> entry : holdingsMap.entrySet()){
			Order order = new Order();
			order.setSec(entry.getKey());
			//System.out.println("modelsMap.get(entry.getKey()):" +modelsMap.get(entry.getKey()));
			Float idealAmountForEntry = (float) (amount*modelsMap.get(entry.getKey())*(0.01));
			//amountDiff = Amount of SEC in Model - Amount of that SEC in Holding
			Float amountDiff = idealAmountForEntry - entry.getValue();      
			if(amountDiff > 0){
				order.setTrans('B');    //set Trans to B-Buy if Model is more than Holding
			}else if(amountDiff < 0){
				order.setTrans('S'); //set Trans to S-Sell
				amountDiff = amountDiff*(-1); //Because the amountDiff is negative, converting it to positive value.
			}
			order.setDiffAmount(amountDiff);
			orders.add(order);
		}
		return orders;
	}
	
	public Float calculateAmount(List<Holding> holdings) {
		float totalAmount = 0;
		
		for(Holding holding: holdings) {
			
			totalAmount = totalAmount + holding.getAmt();
		}
		
		return totalAmount;
	}
	
	
}
