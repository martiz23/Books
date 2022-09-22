package behaviours;

import agents.BookBuyerAgent;
import jade.core.AID;
import jade.core.behaviours.Behaviour;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;

public class RequestPerformer extends Behaviour{
    private AID bestSeller;
    private int bestPrice;
    private int repliesCount = 0;
    private MessageTemplate mt;
    private int step = 0;
    private BookBuyerAgent bbAgent;
    private String bookTitle;
  
    public RequestPerformer(BookBuyerAgent a) {
      bbAgent = a;
      bookTitle = a.getBookTitle();
    }
  
    public void action() {             
        switch(step) {
        case 0:
            if(bookTitle != null){
                ACLMessage cfp = new ACLMessage(ACLMessage.CFP);

                for(int i = 0; i < bbAgent.getSellerAgents().length; i++) {
                  cfp.addReceiver(bbAgent.getSellerAgents()[i]);
                }

                cfp.setContent(bookTitle);
                cfp.setConversationId("book-trade");
                cfp.setReplyWith("cfp" + System.currentTimeMillis());
                myAgent.send(cfp);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                    MessageTemplate.MatchInReplyTo(cfp.getReplyWith()));
                step = 1;
            } else block();
            
        break;

        case 1:
            ACLMessage reply = bbAgent.receive(mt);
            
            if(reply != null ) {
                if(reply.getPerformative() == ACLMessage.PROPOSE) {
                    int price = Integer.parseInt(reply.getContent());
                    if(bestSeller == null || price < bestPrice) {
                        bestPrice = price;
                        bestSeller = reply.getSender();
                    }
                }
                repliesCount++;
                
                if(repliesCount >= bbAgent.getSellerAgents().length) {       
                    if(bestSeller == null){                                                                                   
                        bbAgent.turnDown();                                                      
                    }else                             
                        bbAgent.makeOffer(bestSeller.getName()+": " + bestPrice + "$");
                    step = 2;                    
                }
            } else {                      
                if(bbAgent.getSellerAgents().length == 0){                     
                    bbAgent.turnDown();                        
                }
                block();                
          
            }
        break;

        case 2:
            if(bbAgent.isConfirm()){
                ACLMessage order = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                order.addReceiver(bestSeller);
                order.setContent(bookTitle);
                order.setConversationId("book-trade");
                order.setReplyWith("order" + System.currentTimeMillis());
                bbAgent.send(order);
                //System.out.println("ORDER: "+ order);

                mt = MessageTemplate.and(MessageTemplate.MatchConversationId("book-trade"),
                    MessageTemplate.MatchInReplyTo(order.getReplyWith()));

                step = 3;
            }
        break;

        case 3:      
            reply = myAgent.receive(mt);
            if (reply != null) {
                if (reply.getPerformative() == ACLMessage.INFORM) {
                   System.out.println(bookTitle+" successfully purchased from agent "+reply.getSender().getName());            
                }
                else {
                   System.out.println("Attempt failed: requested book already sold.");
                }
        
                step = 5;              
            }
            else {
               block();
            }
            break;
        case 4:
            
            break;
        }
        
    }
  
    @Override
    public boolean done() {       
        if(step == 5 && bbAgent.isConfirm() || bbAgent.isCancel()){
            bbAgent.restart();
            System.out.println("Operation completed. \n");    
            return true;
        }
        return false;
    }
}
