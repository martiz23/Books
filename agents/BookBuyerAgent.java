package agents;

import jade.core.Agent;
import behaviours.RequestPerformer;
import gui.BookBuyerGui;
import jade.core.AID;
import jade.core.behaviours.*;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

public class BookBuyerAgent extends Agent {
    private String bookTitle;
    private AID[] sellerAgents;
    private int ticker_timer = 2000;
    private BookBuyerAgent this_agent = this;
    
    private boolean confirm;
    private boolean cancel;

    private BookBuyerGui bbGui;            
      
    protected void setup() {
        bbGui = new BookBuyerGui(this);     
        bbGui.showGui();
        
        
        
        System.out.println("Buyer agent " + getAID().getName() + " is ready");
        
        confirm = false;
        //Object[] args = getArguments();
        //if(args != null && args.length > 0) {
          //bookTitle = (String)args[0];
          //System.out.println("Book: " + bookTitle);

        //addBehaviour(new TickerBehaviour(this, ticker_timer) {
        //    protected void onTick() {     
        
        
        //} else {
          //System.out.println("No target book title specified");
          //doDelete();
        //}
    }        
  
    @Override
    protected void takeDown() {       
        System.out.println("Buyer agent " + getAID().getName() + " terminating");
        //cpGui.dispose();
        //bbGui.dispose();
    }        
    
    public void buyBook(String bookTitle){
        this.bookTitle = bookTitle;
        //System.out.println("Book: " + bookTitle);
        addBehaviour(new OneShotBehaviour() {
            public void action() {
                if(bookTitle != null){
                    System.out.println("Trying to buy " + bookTitle);

                    DFAgentDescription template = new DFAgentDescription();
                    ServiceDescription sd = new ServiceDescription();
                    sd.setType("book-selling");
                    template.addServices(sd);

                    try {                    
                        DFAgentDescription[] result = DFService.search(myAgent, template);
                        System.out.println("Found the following seller agents:");
                        String sa = "";
                        sellerAgents = new AID[result.length];
                        for(int i = 0; i < result.length; i++) {
                          sellerAgents[i] = result[i].getName();
                          System.out.println(sellerAgents[i].getName());
                          sa = sa + sellerAgents[i].getName() + " \n";
                        }                                       

                    }catch(FIPAException fe) {
                        fe.printStackTrace();
                    }

                    myAgent.addBehaviour(new RequestPerformer(this_agent));
                    /*
                    if(bestOffer != null){
                        //cpGui.showGui(bestOffer);    
                        bbGui.showConfirmationDialog(bestOffer);
                    }*/
                }
                
            }
        });
    }
    
    /**
     * turns down a buyer
     */
    public void turnDown(){        
        bbGui.showConfirmationDialog();
    }
    
    public void makeOffer(String bestOffer){
        bbGui.showConfirmationDialog(bestOffer);
    }
    
    public void restart(){
        bookTitle = null;      
        cancel = false;
        confirm = false;
    }

    public AID[] getSellerAgents() {
        return sellerAgents;
    }         

    public String getBookTitle() {
      return bookTitle;
    }
        
    public void confirm(){
        this.confirm = true;
    }
    
    public boolean isConfirm(){
        return confirm;
    }          

    public void cancel() {
        this.cancel = true;
    }     
    
    public boolean isCancel() {
        return cancel;
    }
        
}
