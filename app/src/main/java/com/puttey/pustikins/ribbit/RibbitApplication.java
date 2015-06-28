package com.puttey.pustikins.ribbit;

import android.app.Application;

import com.parse.Parse;

/**
 * Created by Pustikins on 6/26/2015.
 */
public class RibbitApplication extends Application{

    @Override
    public void onCreate(){
        super.onCreate();
        // Enable Local Datastore.
        Parse.enableLocalDatastore(this);
        Parse.initialize(this, "ZSTl7uEWmaB4eVWova97tO855uOOTJeYljmksFZ8",
                "EZyHx1YHEkkKp47ejAYqaXdgLWlkBJoDhRNhuXlr");

    }

}
