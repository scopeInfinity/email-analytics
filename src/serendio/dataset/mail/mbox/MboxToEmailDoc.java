package serendio.dataset.mail.mbox;
/****************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one   *
 * or more contributor license agreements.  See the NOTICE file *
 * distributed with this work for additional information        *
 * regarding copyright ownership.  The ASF licenses this file   *
 * to you under the Apache License, Version 2.0 (the            *
 * "License"); you may not use this file except in compliance   *
 * with the License.  You may obtain a copy of the License at   *
 *                                                              *
 *   http://www.apache.org/licenses/LICENSE-2.0                 *
 *                                                              *
 * Unless required by applicable law or agreed to in writing,   *
 * software distributed under the License is distributed on an  *
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY       *
 * KIND, either express or implied.  See the License for the    *
 * specific language governing permissions and limitations      *
 * under the License.                                           *
 ****************************************************************/

import org.apache.james.mime4j.MimeException;
import org.apache.james.mime4j.dom.Body;
import org.apache.james.mime4j.dom.Entity;
import org.apache.james.mime4j.dom.Message;
import org.apache.james.mime4j.dom.MessageBuilder;
import org.apache.james.mime4j.dom.Multipart;
import org.apache.james.mime4j.dom.TextBody;
import org.apache.james.mime4j.message.BodyPart;
import org.apache.james.mime4j.message.DefaultMessageBuilder;
import org.apache.james.mime4j.stream.Field;

import serendio.Utils.Utils;
import serendio.dataset.process.EmailDoc;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;


//import javax.mail.Address;
//import javax.mail.Header;

/**
 * Simple example of how to use Apache Mime4j Mbox Iterator. We split one mbox file file into
 * individual email messages.
 */
public class MboxToEmailDoc {
	private ArrayList<BodyPart> attachments;
 //   private final static CharsetEncoder ENCODER = Charset.forName("UTF-8").newEncoder();

  /*  // simple example of how to split an mbox into individual files
    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            System.out.println("Please supply a path to an mbox file to parse");
        }
private ArrayList<BodyPart> attachments;        
    	//args[0] = "/home/nishant/Serendio/smaple mail dataset/testlist.mbox";
      //  final File mbox = new File(args[0]);
    	  final File mbox = new File("/home/nishant/Serendio/smaple mail dataset/testlist.mbox");
        long start = System.currentTimeMillis();
        int count = 0;

        for (CharBufferWrapper message : MboxIterator.fromFile(mbox).charset(ENCODER.charset()).build()) {
            // saveMessageToFile(count, buf);
            System.out.println(messageSummary(message.asInputStream(ENCODER.charset())));
            count++;
        }
        System.out.println("Found " + count + " messages");
        long end = System.currentTimeMillis();
        System.out.println("Done in: " + (end - start) + " milis");
    }
    */
  /*  public void printMbox(String inputPath) throws FileNotFoundException, IOException, MimeException
    {
    	File mbox = new File(inputPath);
    	for(CharBufferWrapper message : MboxIterator.fromFile(mbox).charset(ENCODER.charset()).build())
    	{
    	//	System.out.println(messageSummary(message.asInputStream(ENCODER.charset())));
    	//	break;
    	}
    }*/
    
/*    public void saveMessageToFile(int count, CharBuffer buf) throws IOException {
        FileOutputStream fout = new FileOutputStream(new File("target/messages/msg-" + count));
        FileChannel fileChannel = fout.getChannel();
        ByteBuffer buf2 = ENCODER.encode(buf);
        fileChannel.write(buf2);
        fileChannel.close();
        fout.close();
    }
*/
    public String getTxtPart(Body body) throws IOException {    
        TextBody tb = (TextBody) body;  
        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
        tb.writeTo(baos);  
        return new String(baos.toByteArray());  
    }  
 
    /**
     * Parse a message and return a simple {@link String} representation of some important fields.
     *
     * @param messageBytes the message as {@link java.io.InputStream}
     * @return String
     * @throws IOException
     * @throws MimeException
     */
    public String getNameFromHeader(Message message)
    {
    //	Iterator<Field> headers = message.getHeader().iterator();
    	
		String Name = null;
		
	//	while(headers.hasNext())
		
		//	String candidateField = headers.next().getName();
		//	System.out.println(candidateField);
		//	System.out.println(message.getHeader().getField("Sender").getBody());
			if(message.getHeader().getField("From").getBody() != null)
			{
				String listName[] = message.getHeader().getField("From").getBody().split("<");
				if(listName[0].length()>1)
					Name = listName[0];
			}
			System.out.println(Name);
//			if(candidateField.contains("Sender"))
//			{
				
//			}
			/*
			if(h.getName().contains("X-From") || h.getName().contains("Sender"))
				Name = h.getValue();
			if(h.getName().contains("From:"))
			{
				String listName[] = h.getName().split("<");
				if(listName[0].length()>1)
					Name = listName[0];
			}
//			else if(h.getName().contains("Sender"))
				*/
	//	}
		
		return Name;
    }
    public EmailDoc messageToemailDoc(InputStream messageBytes) throws MimeException, IOException
    {
    	/*
    	 * TO Do: Content Not Added from corp to object
    	 * */	
        MessageBuilder builder = new DefaultMessageBuilder();
        Message message = builder.parseMessage(messageBytes);
        EmailDoc emailObject = new EmailDoc();
      //  Address[] tempAddress = null;
        emailObject.setMessage_ID(message.getMessageId());
        emailObject.setName(getNameFromHeader(message));
        emailObject.setFrom(message.getFrom().get(0).getAddress().toString());
        emailObject.setSubject(message.getSubject());
        emailObject.setDate(message.getDate().toString());
        if(message.getBcc() != null)
        {
        //	message.getBcc().toArray(tempAddress);
        	emailObject.setBcc(Utils.addressListToHashset(message.getBcc()));
        }
        
        if(message.getCc() != null)
        {
        //	message.getCc().toArray(tempAddress);
        	emailObject.setCc(Utils.addressListToHashset(message.getCc()));
        }
        
        if(message.getTo() != null)
        {
        	//message.getTo().toArray(tempAddress);
        //	tempAddress =  message.getTo().toArray(new Address[message.getTo().size()]);
        //	System.out.println(tempAddress);
        	emailObject.setTo(Utils.addressListToHashset(message.getTo()));
        }
        emailObject.setContent(getTextPart(message));
        /*
        if(message.isMultipart())
        {
        	Multipart multipart = (Multipart) message.getBody();
        	
        	parseBodyParts(multipart);
        }
        else
        {
        	String text = getTextPart(message);
        	emailObject.setContent(text);
        }
        */
    	return emailObject;
    }
/*
	private void parseBodyParts(Multipart multipart) {
		// TODO Auto-generated method stub
		List<Entity> list = multipart.getBodyParts();
		
		Iterator<Entity> it = list.iterator();
		
		
		for(BodyPart part : multipart.getBodyParts())
		{
			if(part.isMimeType("text/plain")) 
			{  
                String txt = getTextPart(part);  
                //txtBody.append(txt);  
            } 
			else if(part.isMimeType("text/html")) 
			{  
                String html = getTextPart(part);  
                //htmlBody.append(html);  
            } 
			else if (part.getDispositionType() != null && !part.getDispositionType().equals("")) 
			{  
                //If DispositionType is null or empty, it means that it's multipart, not attached file  
                attachments.add(part);  
            }
			if(part.isMultipart()) 
			{  
	                parseBodyParts((Multipart) part.getBody());  
	        }  
		
		}
		
	}
*/
	private String getTextPart(Entity part) throws IOException {
		// TODO Auto-generated method stub
			TextBody tb = (TextBody) part.getBody();  
	        ByteArrayOutputStream baos = new ByteArrayOutputStream();  
	        tb.writeTo(baos);  
	        return new String(baos.toByteArray()); 
	}
    
    /*
    public String messageSummary(InputStream messageBytes) throws IOException, MimeException {
        MessageBuilder builder = new DefaultMessageBuilder();
        Message message = builder.parseMessage(messageBytes);
        return String.format("\nFrom %s \n" +
        	     "To:\t%s\n" +
                 "Cc:\t%s\n"+
                 "Bcc:\t%s\n"+
                 "Subject:\t%s\n"+
                 "Contant:\t\n"+
                 "Date:\t%s\n",
                 message.getSender(),
                 message.getTo(),
                 message.getCc(),
                 message.getBcc(),
                 message.getSubject(),
               //  getTxtPart(message.getBody()),
                 message.getDate()+"\nattachment Name:"+message.getFilename());
 
        return String.format("%s \n",
                message.getHeader()
               // +"\nAttachment: "+message.getFilename()
              //  +"\nBody"+getTxtPart(message.getBody())
                );
    }
*/}
