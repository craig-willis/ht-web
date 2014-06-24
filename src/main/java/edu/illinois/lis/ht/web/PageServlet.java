package edu.illinois.lis.ht.web;

import gov.loc.repository.pairtree.Pairtree;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class PageServlet extends HttpServlet 
{
	private static final long serialVersionUID = 1L;

	public void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException 
    {
    	String volumeId = request.getParameter("volumeId");
    	String pageId = request.getParameter("pageId");
    	
		volumeId = URLDecoder.decode(volumeId, "UTF-8");
		Pairtree pt = new Pairtree();
		
        String sourcePart = volumeId.substring(0, volumeId.indexOf("."));
        String volumePart = volumeId.substring(volumeId.indexOf(".")+1, volumeId.length());
        String uncleanId = pt.uncleanId(volumePart);
        String path = pt.mapToPPath(uncleanId);
        String cleanId = pt.cleanId(volumePart);
        
        String ocrBasePath= "/data/hathi0/gpd/combined";
        
        String zipPath = ocrBasePath + File.separator + sourcePart 
        		+ File.separator + "pairtree_root" 
        		+ File.separator + path 
        		+ File.separator + cleanId
        		+ File.separator + cleanId + ".zip";
        
        ZipFile zipFile = new ZipFile(zipPath);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        
	    while(entries.hasMoreElements()){
	        ZipEntry entry = entries.nextElement();
	
	    	if (entry.isDirectory())
	        	continue;
		    		   
	        String fileName = entry.getName();
	        String dirPart = fileName.substring(0, fileName.indexOf("/"));
	        String namePart = fileName.substring( fileName.indexOf("/") + 1, fileName.length());
	        // Skip the concatenated version of the file
	        if (namePart.equals(dirPart + ".txt")) 
	        	continue;
	        String pageNum = namePart.replaceAll("\\..*", "");
	        if (pageId.equals(pageNum))
	        {
		        
		        String text = "";
		        InputStream is = zipFile.getInputStream(entry);
		        
		        BufferedReader br = new BufferedReader(new InputStreamReader(is));
		        String line;
		        while ((line = br.readLine()) != null) 
		        	text += line + "\n";
		        
		        response.setContentType("text");
		        PrintWriter out = response.getWriter();
		        try {
		            out.println( text );
		        } catch( Exception e ) {
		            out.println( "Error Retrieving Forecast: " + e.getMessage() );
		        }
		        out.flush();
		        out.close();
		        
		        
		        break;
		    }
	        
	    }
    }
}