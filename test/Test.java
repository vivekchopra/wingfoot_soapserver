import com.wingfoot.soap.*;
import com.wingfoot.soap.transport.*;

public class Test {
    
    public static void main(String a[]) throws Exception {
        
	new Test().init();
    }

    public void init() throws Exception {
        
	Call call = new Call();
        //String str = "\"&<>\";\"";      
        String str = "Kal Iyer";      
	//call.addParameter("name", str);
	call.addParameter("name",null);
        
	call.setMethodName("getGreeting");
	call.setTargetObjectURI("urn:www.kaliyer.com");
	
        Transport transport = new J2SEHTTPTransport 
	         ("http://localhost:8080/wingfoot/servlet/wserver", "");
        Envelope response = call.invoke(transport);
        if (response.isFaultGenerated()) {
             Fault f = response.getFault();
	     System.err.println(f.getFaultCode());
	     System.err.println(f.getFaultString());
	     System.err.println(f.getDetail());

	}
	System.err.println("*"+response.getParameterName(0)+"*");
	System.err.println("*"+response.getParameter(0)+"*");
	System.err.println("*"+response.getMethodName()+"*");
	System.err.println("*"+response.getTargetURI()+"*");
    }

} /* Test */
