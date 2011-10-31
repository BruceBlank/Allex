package de.bruceblank.allex;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLEncoder;

import org.htmlcleaner.CleanerProperties;
import org.htmlcleaner.HtmlCleaner;
import org.htmlcleaner.TagNode;
import org.htmlcleaner.XPatherException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;

import android.webkit.WebView;

public class HtmlSourceActivity extends Activity {
	// for logging
	private final String TAG = this.getClass().getSimpleName();
	private ProgressDialog loadingDialog;
	// TODO: change title of HtmlSourceActivity to text in title-TAG of Html
	private String htmlSourceTitle = this.getString(R.string.htmlsource);
	
	// called on create
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"onCreate called");
        
        // TODO: remove this
        // want to show an indeterminate progress bar
        //requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        
        setContentView(R.layout.htmlsourceactivity);  

        // read URL from intend
        Bundle bundle = getIntent().getExtras();
        String url = bundle.getString("URL");
        
        // Makes Progress bar Visible
        //TODO: remove this        
        //setProgressBarIndeterminateVisibility(true);
        
        // show a progress dialog while loading
        //TODO: what todo, when user presses back? 
        loadingDialog = ProgressDialog.show(this, getString(R.string.LoadingDialogTitle), getString(R.string.LoadingDialogText) ); 
        
        // set WebView content from other thread
        Log.i(TAG, "showing web page");    
        new DownloadParagraphPageTask().execute(url);
    }
    
    // the AsyncTask-class to load the paragraph page
    private class DownloadParagraphPageTask extends AsyncTask<String, Integer, Boolean> {
    	String pageSource;
    
    	// this will be done in a second thread
        protected Boolean doInBackground(String... urls) {
        	if(urls.length!=1) return false;
        	pageSource = composeParagraphPage(urls[0]); 	
        	return true;
        }
        
        // this will be done in the UI-thread after the second thread finishes
        protected void onPostExecute(Boolean result) {
        	if(result){
        		((WebView) findViewById(R.id.webview)).loadData(pageSource, "text/html", "iso-8859-1");
        		// TODO: set title of webview page to htmlSourceTitle
        	}
        	// Turn off progress bar
            //setProgressBarIndeterminateVisibility(false);
            //TODO: remove this
        	
        	// remove the progress dialog
        	loadingDialog.dismiss();
        }
    }
    
    // compose an html-page with the necessary informations from url only
    private String composeParagraphPage(final String url){
    	String result;
    	
    	String htmlParagraph="", htmlTitle="", htmlText="";
    	
    	// create a HTMLcleaner and set some properties
    	HtmlCleaner htmlCleaner = new HtmlCleaner();
    	CleanerProperties props = htmlCleaner.getProperties();
    	props.setAllowHtmlInsideAttributes(true);
    	props.setAllowMultiWordAttributes(true);
    	props.setRecognizeUnicodeChars(false);
    	props.setOmitComments(true);

    	// TODO: what about htmlCleanerException?
    	try {
    		// parse the HTML source and create an internal data-structure
    		TagNode node = htmlCleaner.clean(new URL(url));
    		
    		// search for the specified CSS-classes
    		// TODO: maybe these should be settings
    		final String xPathExpParagraph = "//span[@class='jnenbez']";
    		final String xPathExpTitle = "//span[@class='jnentitel']";
    		final String xPathExpText = "//div[@class='jnhtml']";
    		try {
    			Object[] NodesParagraph = node.evaluateXPath(xPathExpParagraph);
    			Object[] NodesTitle = node.evaluateXPath(xPathExpTitle);
    			Object[] NodesText = node.evaluateXPath(xPathExpText);
    			// every evaluteXPath should return exactly one Object
    			if(NodesParagraph.length==1 && NodesTitle.length==1 && NodesText.length==1){
    				htmlParagraph = htmlCleaner.getInnerHtml((TagNode)(NodesParagraph[0]));
    				htmlTitle = htmlCleaner.getInnerHtml((TagNode)(NodesTitle[0]));
    				htmlText = htmlCleaner.getInnerHtml((TagNode)(NodesText[0]));
    			}else{
    				Log.e(TAG,"ERROR: at least one node of paragraph, title or text not found");
    			}
    		} catch(XPatherException e) {
    			Log.e(TAG, "XPatherException: " + e.getMessage());
    		}
    	} catch (IOException e) {
    	     Log.e(TAG, "IOException: " + e.getMessage());
    	}

    	// compose an html page from the strings
    	result = composeParagraphPage(htmlParagraph, htmlTitle, htmlText);
    	
    	return result;
    }
 
    // compose an html page from the strings para, title and text
    private String composeParagraphPage(String para, String title, String text){
    	String result;
    	
    	// to prevent error messages (e.g. in GG)
    	if(para == null) para = "";
    	if(title == null) title = "";
    	if(text == null) text = "";
    	
    	if(para=="" && title=="" && text=="") return composeErrorPage();
    	
    	// construct html
    	result =  "<html><head>\n" +
    		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" +
    		"<title>Einzelnorm</title>" +
    		"</head>\n" +
    		"<body>\n" +
    		"<style type=\"text/css\">\n" +
    		"body { background-color:#DDDDDD; }" +
    		"* { font-family:Helvetica,Arial,sans-serif; }" +
    		"h1 { font-size:1.3em; text-align:left; }\n" +
    		"</style>\n" +
    		"<h1>" + para + " " + title + "</h1>\n" +
    		text + "\n" +
    		"</body></html>\n";
    	
    	// try encoding the data (dont know, why its necessary, but it is)
    	try{
    		result = URLEncoder.encode(result,"iso-8859-1").replaceAll("\\+"," ");
    	}catch(UnsupportedEncodingException e){
    		// TODO: remove this, an error code should be returned instead
    		result = composeErrorPage();
    	}
    	
    	return result;
    }

    // compose an error page, when the real page cannot be encoded
    private String composeErrorPage(){
    	String result;
    	
    	result =  "<html><head>\n" +
		"<meta http-equiv=\"Content-Type\" content=\"text/html; charset=iso-8859-1\" />\n" +
		"<title>Einzelnorm</title>" +
		"</head>\n" +
		"<body>\n" +
		"<h1 style=\"color:red; font-size:1.3em\">Page cannot be shown</h1>\n" +
		"</body></html>\n";

    	return result;
    }
}
