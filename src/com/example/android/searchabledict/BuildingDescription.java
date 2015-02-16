package com.example.android.searchabledict;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

public class BuildingDescription extends Activity {

    private TextView mBuilding;
    private TextView mDefinition;
    private TextView mAbbr;
    private ImageView mImage;
    private TextView mDescription;
    private Button btnGo;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.description);

        mBuilding = (TextView) findViewById(R.id.building);
        mDefinition = (TextView) findViewById(R.id.definition);
        mAbbr = (TextView) findViewById(R.id.abbr);
        mImage = (ImageView) findViewById(R.id.photo);
        mDescription = (TextView) findViewById(R.id.description);
        btnGo = (Button) findViewById(R.id.go_button);
        
        
        
        Intent intent = getIntent();

        String building = intent.getStringExtra("building");
        String definition = intent.getStringExtra("definition");
        String abbr = "Abbreviation: " + intent.getStringExtra("abbr");
        String description = "Category: " + intent.getStringExtra("description");
        String imagename = intent.getStringExtra("imagename");
        
        mBuilding.setText(building);
        mDefinition.setText(definition);

        String fullDesc = "";
        if(!abbr.equals("Abbreviation: XXXXX"))
        {
        	fullDesc += (abbr + "\n");
        }
        if(!description.equals("Category: XXXXX"))
        {
        	fullDesc += (description + "\n");
        }
        fullDesc += ("Building Code: "+definition+"\n");
        mDescription.setText(fullDesc);        
 
        btnGo.setOnClickListener(new View.OnClickListener() {
          	 
            public void onClick(View arg0) {
                //Starting a new Intent
                Intent nextScreen = new Intent(getApplicationContext(), MapDisplay.class);
 
                nextScreen.putExtra("building_name", mBuilding.getText());
                nextScreen.putExtra("buiding_code", mDefinition.getText());
                
                //set global singleton to the name of the building
       		 	globalStrings.nameSelectedBuilding = (String)mBuilding.getText();
       		 	//set global path drawing state to one for on
       		 	globalStrings.pathDrawingState =1;
       		 	//set the webservice call boolean to on
       		 	globalStrings.webserviceCallBool=1;
                
       		 	startActivity(nextScreen);
                finish();
 
            }
        });  


        //Following provides corresponding image for each building

        if(imagename.equals("abbot"))
        {
        	mImage.setImageResource(R.drawable.abbot);
        }
        else if(imagename.equals("abrams"))
        {
        	mImage.setImageResource(R.drawable.abrams);
        }
        else if(imagename.equals("hannah"))
        {
        	mImage.setImageResource(R.drawable.hannah);
        }
        else if(imagename.equals("farrall"))
        {
        	mImage.setImageResource(R.drawable.farrall);
        }
        else if(imagename.equals("agriculture"))
        {
        	mImage.setImageResource(R.drawable.agriculture);
        }
        else if(imagename.equals("akers"))
        {
        	mImage.setImageResource(R.drawable.akers);
        }
        else if(imagename.equals("alumni_chapel"))
        {
        	mImage.setImageResource(R.drawable.alumni_chapel);
        }
        else if(imagename.equals("angell"))
        {
        	mImage.setImageResource(R.drawable.angell);
        }
        else if(imagename.equals("anthony_hall"))
        {
        	mImage.setImageResource(R.drawable.anthony_hall);
        }
        else if(imagename.equals("armstrong"))
        {
        	mImage.setImageResource(R.drawable.spartan2);
        }
        else if(imagename.equals("auditorium"))
        {
        	mImage.setImageResource(R.drawable.auditorium);
        }
        else if(imagename.equals("bailey_hall"))
        {
        	mImage.setImageResource(R.drawable.bailey_hall);
        }
        else if(imagename.equals("baker"))
        {
        	mImage.setImageResource(R.drawable.baker);
        }
        else if(imagename.equals("beal_garden"))
        {
        	mImage.setImageResource(R.drawable.beal_garden);
        }
        else if(imagename.equals("beaumont_tower"))
        {
        	mImage.setImageResource(R.drawable.beaumont_tower);
        }
        else if(imagename.equals("berkey"))
        {
        	mImage.setImageResource(R.drawable.berkey);
        }
        else if(imagename.equals("bessey"))
        {
        	mImage.setImageResource(R.drawable.bessey);
        }
        else if(imagename.equals("biochem"))
        {
        	mImage.setImageResource(R.drawable.biochem);
        }
        else if(imagename.equals("biomedical_physical_science"))
        {
        	mImage.setImageResource(R.drawable.biomedical_physical_science);
        }
        else if(imagename.equals("breslin"))
        {
        	mImage.setImageResource(R.drawable.breslin);
        }
        else if(imagename.equals("brody"))
        {
        	mImage.setImageResource(R.drawable.spartan2);
        }
        else if(imagename.equals("bryan_hall"))
        {
        	mImage.setImageResource(R.drawable.bryan_hall);
        }
        else if(imagename.equals("bcc"))
        {
        	mImage.setImageResource(R.drawable.bcc);
        }
        else if(imagename.equals("butterfield_hall"))
        {
        	mImage.setImageResource(R.drawable.butterfield_hall);
        }
        else if(imagename.equals("campbell"))
        {
        	mImage.setImageResource(R.drawable.campbell);
        }
        else if(imagename.equals("case_hall"))
        {
        	mImage.setImageResource(R.drawable.case_hall);
        }
        else if(imagename.equals("centralserv"))
        {
        	mImage.setImageResource(R.drawable.centralserv);
        }
        else if(imagename.equals("plantsys"))
        {
        	mImage.setImageResource(R.drawable.plantsys);
        }
        else if(imagename.equals("chemistry"))
        {
        	mImage.setImageResource(R.drawable.chemistry);
        }
        else if(imagename.equals("chittenden"))
        {
        	mImage.setImageResource(R.drawable.chittenden);
        }
        else if(imagename.equals("clinicalcenter"))
        {
        	mImage.setImageResource(R.drawable.clinicalcenter);
        }
        else if(imagename.equals("communication"))
        {
        	mImage.setImageResource(R.drawable.communication);
        }
        else if(imagename.equals("computer_center"))
        {
        	mImage.setImageResource(R.drawable.computer_center);
        }
        else if(imagename.equals("conrad"))
        {
        	mImage.setImageResource(R.drawable.conrad);
        }
        else if(imagename.equals("cook"))
        {
        	mImage.setImageResource(R.drawable.cook);
        }
        else if(imagename.equals("cowles_house"))
        {
        	mImage.setImageResource(R.drawable.cowles_house);
        }
        else if(imagename.equals("cyclotron"))
        {
        	mImage.setImageResource(R.drawable.cyclotron);
        }
        else if(imagename.equals("demonstration"))
        {
        	mImage.setImageResource(R.drawable.demonstration);
        }
        else if(imagename.equals("daugherty"))
        {
        	mImage.setImageResource(R.drawable.daugherty);
        }
        else if(imagename.equals("emmons_hall"))
        {
        	mImage.setImageResource(R.drawable.emmons_hall);
        }
        else if(imagename.equals("engineering"))
        {
        	mImage.setImageResource(R.drawable.engineering);
        }
        else if(imagename.equals("engresearch"))
        {
        	mImage.setImageResource(R.drawable.engresearch);
        }
        else if(imagename.equals("eppley"))
        {
        	mImage.setImageResource(R.drawable.eppley);
        }
        else if(imagename.equals("erickson"))
        {
        	mImage.setImageResource(R.drawable.erickson);
        }
        else if(imagename.equals("eustace_cole"))
        {
        	mImage.setImageResource(R.drawable.eustace_cole);
        }
        else if(imagename.equals("fee"))
        {
        	mImage.setImageResource(R.drawable.fee);
        }
        else if(imagename.equals("fire"))
        {
        	mImage.setImageResource(R.drawable.fire);
        }
        else if(imagename.equals("food_safety"))
        {
        	mImage.setImageResource(R.drawable.food_safety);
        }
        else if(imagename.equals("foodsci"))
        {
        	mImage.setImageResource(R.drawable.foodsci);
        }
        else if(imagename.equals("foodstores"))
        {
        	mImage.setImageResource(R.drawable.foodstores);
        }
        else if(imagename.equals("geography"))
        {
        	mImage.setImageResource(R.drawable.geography);
        }
        else if(imagename.equals("gilchrist"))
        {
        	mImage.setImageResource(R.drawable.gilchrist);
        }
        else if(imagename.equals("giltner"))
        {
        	mImage.setImageResource(R.drawable.giltner);
        }
        else if(imagename.equals("olin"))
        {
        	mImage.setImageResource(R.drawable.olin);
        }
        else if(imagename.equals("holden"))
        {
        	mImage.setImageResource(R.drawable.holden);
        }
        else if(imagename.equals("holmes"))
        {
        	mImage.setImageResource(R.drawable.holmes);
        }
        else if(imagename.equals("hubbard"))
        {
        	mImage.setImageResource(R.drawable.hubbard);
        }
        else if(imagename.equals("humanecology"))
        {
        	mImage.setImageResource(R.drawable.humanecology);
        }
        else if(imagename.equals("imcircle"))
        {
        	mImage.setImageResource(R.drawable.imcircle);
        }
        else if(imagename.equals("imeast"))
        {
        	mImage.setImageResource(R.drawable.imeast);
        }
        else if(imagename.equals("im_sports_west"))
        {
        	mImage.setImageResource(R.drawable.im_sports_west);
        }
        else if(imagename.equals("munn"))
        {
        	mImage.setImageResource(R.drawable.munn);
        }
        else if(imagename.equals("tennis1"))
        {
        	mImage.setImageResource(R.drawable.tennis1);
        }
        else if(imagename.equals("instrmedia"))
        {
        	mImage.setImageResource(R.drawable.instrmedia);
        }
        else if(imagename.equals("internatcen"))
        {
        	mImage.setImageResource(R.drawable.internatcen);
        }
        else if(imagename.equals("jenison"))
        {
        	mImage.setImageResource(R.drawable.jenison);
        }
        else if(imagename.equals("kedzie"))
        {
        	mImage.setImageResource(R.drawable.kedzie);
        }
        else if(imagename.equals("south_kedzie_hall"))
        {
        	mImage.setImageResource(R.drawable.south_kedzie_hall);
        }
        else if(imagename.equals("kellogg_center"))
        {
        	mImage.setImageResource(R.drawable.kellogg_center);
        }
        else if(imagename.equals("kresge"))
        {
        	mImage.setImageResource(R.drawable.kresge);
        }
        else if(imagename.equals("landon"))
        {
        	mImage.setImageResource(R.drawable.landon);
        }
        else if(imagename.equals("laundry"))
        {
        	mImage.setImageResource(R.drawable.laundry);
        }
        else if(imagename.equals("college_of_law"))
        {
        	mImage.setImageResource(R.drawable.college_of_law);
        }
        else if(imagename.equals("library"))
        {
        	mImage.setImageResource(R.drawable.library);
        }
        else if(imagename.equals("life"))
        {
        	mImage.setImageResource(R.drawable.life);
        }
        else if(imagename.equals("linton"))
        {
        	mImage.setImageResource(R.drawable.linton);
        }
        else if(imagename.equals("museum"))
        {
        	mImage.setImageResource(R.drawable.museum);
        }
        else if(imagename.equals("union"))
        {
        	mImage.setImageResource(R.drawable.union);
        }
        else if(imagename.equals("manlymiles"))
        {
        	mImage.setImageResource(R.drawable.manlymiles);
        }
        else if(imagename.equals("marshall_adams"))
        {
        	mImage.setImageResource(R.drawable.marshall_adams);
        }
        else if(imagename.equals("mason"))
        {
        	mImage.setImageResource(R.drawable.mason);
        }
        else if(imagename.equals("marymayo"))
        {
        	mImage.setImageResource(R.drawable.marymayo);
        }
        else if(imagename.equals("mcdonel"))
        {
        	mImage.setImageResource(R.drawable.mcdonel);
        }
        else if(imagename.equals("morrill"))
        {
        	mImage.setImageResource(R.drawable.morrill);
        }
        else if(imagename.equals("music"))
        {
        	mImage.setImageResource(R.drawable.music);
        }
        else if(imagename.equals("music_practice"))
        {
        	mImage.setImageResource(R.drawable.music_practice);
        }
        else if(imagename.equals("natural_resources"))
        {
        	mImage.setImageResource(R.drawable.natural_resources);
        }
        else if(imagename.equals("natural_science"))
        {
        	mImage.setImageResource(R.drawable.natural_science);
        }
        else if(imagename.equals("nisbet"))
        {
        	mImage.setImageResource(R.drawable.nisbet);
        }
        else if(imagename.equals("observatory"))
        {
        	mImage.setImageResource(R.drawable.observatory);
        }
        else if(imagename.equals("old_botany"))
        {
        	mImage.setImageResource(R.drawable.old_botany);
        }
        else if(imagename.equals("oldhort"))
        {
        	mImage.setImageResource(R.drawable.oldhort);
        }
        else if(imagename.equals("olds"))
        {
        	mImage.setImageResource(R.drawable.olds);
        }
        else if(imagename.equals("owen"))
        {
        	mImage.setImageResource(R.drawable.owen);
        }
        else if(imagename.equals("speechhear"))
        {
        	mImage.setImageResource(R.drawable.speechhear);
        }
        else if(imagename.equals("packaging"))
        {
        	mImage.setImageResource(R.drawable.packaging);
        }
        else if(imagename.equals("pavilion"))
        {
        	mImage.setImageResource(R.drawable.pavilion);
        }
        else if(imagename.equals("phillips"))
        {
        	mImage.setImageResource(R.drawable.phillips);
        }
        else if(imagename.equals("physplnt"))
        {
        	mImage.setImageResource(R.drawable.physplnt);
        }
        else if(imagename.equals("plantb"))
        {
        	mImage.setImageResource(R.drawable.plantb);
        }
        else if(imagename.equals("plantscigrnhs"))
        {
        	mImage.setImageResource(R.drawable.plantscigrnhs);
        }
        else if(imagename.equals("plantsoil"))
        {
        	mImage.setImageResource(R.drawable.plantsoil);
        }
        else if(imagename.equals("pubsafety"))
        {
        	mImage.setImageResource(R.drawable.pubsafety);
        }
        else if(imagename.equals("simonpp"))
        {
        	mImage.setImageResource(R.drawable.simonpp);
        }
        else if(imagename.equals("psych_bldg"))
        {
        	mImage.setImageResource(R.drawable.psych_bldg);
        }
        else if(imagename.equals("radiology"))
        {
        	mImage.setImageResource(R.drawable.radiology);
        }
        else if(imagename.equals("rather_hall"))
        {
        	mImage.setImageResource(R.drawable.rather_hall);
        }
        else if(imagename.equals("shaw"))
        {
        	mImage.setImageResource(R.drawable.shaw);
        }
        else if(imagename.equals("snyder"))
        {
        	mImage.setImageResource(R.drawable.snyder);
        }
        else if(imagename.equals("soilscires"))
        {
        	mImage.setImageResource(R.drawable.soilscires);
        }
        else if(imagename.equals("stadium"))
        {
        	mImage.setImageResource(R.drawable.stadium);
        }
        else if(imagename.equals("svillage_comcenter"))
        {
        	mImage.setImageResource(R.drawable.svillage_comcenter);
        }
        else if(imagename.equals("student"))
        {
        	mImage.setImageResource(R.drawable.student);
        }
        else if(imagename.equals("surplus"))
        {
        	mImage.setImageResource(R.drawable.surplus);
        }
        else if(imagename.equals("tennis"))
        {
        	mImage.setImageResource(R.drawable.tennis);
        }
        else if(imagename.equals("uvillage"))
        {
        	mImage.setImageResource(R.drawable.uvillage);
        }
        else if(imagename.equals("vanhoosen"))
        {
        	mImage.setImageResource(R.drawable.vanhoosen);
        }
        else if(imagename.equals("vetmed"))
        {
        	mImage.setImageResource(R.drawable.vetmed);
        }
        else if(imagename.equals("visitor_information"))
        {
        	mImage.setImageResource(R.drawable.visitor_information);
        }
        else if(imagename.equals("wells"))
        {
        	mImage.setImageResource(R.drawable.wells);
        }
        else if(imagename.equals("wharton"))
        {
        	mImage.setImageResource(R.drawable.wharton);
        }
        else if(imagename.equals("williams"))
        {
        	mImage.setImageResource(R.drawable.williams);
        }
        else if(imagename.equals("willshouse"))
        {
        	mImage.setImageResource(R.drawable.willshouse);
        }
        else if(imagename.equals("wilson"))
        {
        	mImage.setImageResource(R.drawable.wilson);
        }
        else if(imagename.equals("wonders"))
        {
        	mImage.setImageResource(R.drawable.wonders);
        }
        else if(imagename.equals("yakeley"))
        {
        	mImage.setImageResource(R.drawable.yakeley);
        }
        else
        {
        	mImage.setImageResource(R.drawable.spartan2);
        }
                
    }	
}
