package de.bruceblank.allex;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.Button;
import android.view.View;
import android.util.Log;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;

public class AllexActivity extends Activity {
	// for logging
	private final String TAG = this.getClass().getSimpleName();
	// TODO: move IndexURL and ParagraphURL to a setting
	private final String IndexURL = "http://www.gesetze-im-internet.de/%s/index.html";
	private final String ParagraphURL = "http://www.gesetze-im-internet.de/%s/%n.html";
	
	// called on create
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Log.i(TAG,"onCreate called");
        
        // find UI elements
        final EditText etLaw = (EditText) findViewById(R.id.editTextLaw);
        final EditText etPara = (EditText) findViewById(R.id.editTextParagraph);
        final Button buttonSearch = (Button) findViewById(R.id.buttonSearch);
        final Button buttonIndex = (Button) findViewById(R.id.buttonIndex);
           
        // search-button callback
        buttonSearch.setOnClickListener(new Button.OnClickListener(){
        	@Override public void onClick(View v){
        		// create link
        		final String link = composeSearchURL(etLaw.getText().toString(), etPara.getText().toString());
        		Log.i(TAG,"Search-Button clicked\n" + "Link: " + link);
        		// start Browser
        		//startBrowser(link);
        		// start new activity to view the html source code
        		Bundle bundle = new Bundle();
        		bundle.putString("URL", link);
        		Intent intentHtmlSource = new Intent(AllexActivity.this, HtmlSourceActivity.class);
        		intentHtmlSource.putExtras(bundle);
                startActivity(intentHtmlSource);
        	}
        });

        // index-button callback
        buttonIndex.setOnClickListener(new Button.OnClickListener(){
        	@Override public void onClick(View v){
        		// create link
        		final String link = composeIndexURL(etLaw.getText().toString());
        		Log.i(TAG,"Index-Button clicked\n" + "Link: " + link);
        		// start Browser
        		startBrowser(link);
        	}
        });

    }

    // show a messag box with no buttons and the specified message
	private void showMessageBox(final String message) {
		AlertDialog alertDialog;
		alertDialog = new AlertDialog.Builder(this).create();
		alertDialog.setTitle(TAG);
		alertDialog.setMessage(message);
		alertDialog.show();
	}

	// start browser, showing the specified URL
	private void startBrowser(final String url){
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);

	}
	
	// replace some characters in s to make string URL-conform
	private String urify(final String s){
		String result = s.replace(' ', '_');
		result = result.replace('.', '_'); 
		result = result.replace('%', '_');
		result = result.replace('/', '_'); 
		return result;
	}
	
	// compose the URL for the index of the law in lawstr
	private String composeIndexURL(final String lawstr){
		String result = IndexURL.replace("%s", urify(lawstr)); 
		
		return result;
	}

	// compose the URL for the index of the law in lawstr
	private String composeSearchURL(final String lawstr, final String parastr){
		String parapart;
		// this is a speacial handling for the "Grundgesetz-Artikel"
		if(lawstr.equalsIgnoreCase("gg")){
			parapart = "art_" + urify(parastr);
		}else{
			parapart = "__" + urify(parastr);
		}
		
		String result = ParagraphURL.replace("%s", urify(lawstr)).replace("%n", parapart); 
		
		return result;
	}
}