package com.appboy.segment.appboysample;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

public class MainFragment extends Fragment {

  Random mRandom = new Random();

  public MainFragment() {}

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = layoutInflater.inflate(R.layout.content_main, container, false);

    Button identifyButton = view.findViewById(R.id.identifyButton);
    identifyButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        EditText userIDTextField = view.findViewById(R.id.userIDField);
        String newUser = Integer.toString(Math.abs(mRandom.nextInt()));
        if (userIDTextField.getText().toString().length() > 0) {
          newUser = userIDTextField.getText().toString();
        }
        TextView textView = view.findViewById(R.id.helloText);
        textView.setText("Hello Appboy and Segment! Current User: " + newUser);
        Toast.makeText(getContext(), "identify() called with user id: " + newUser + ".", Toast.LENGTH_LONG).show();
        Traits traits = new Traits();
        Date birthday = new Date(System.currentTimeMillis());
        traits.putBirthday(birthday);
        traits.putEmail("abc@123.com");
        traits.putFirstName("First");
        traits.putLastName("Last");
        traits.putGender("m");
        traits.putPhone("5555555555");
        Traits.Address address = new Traits.Address();
        address.putCity("City");
        address.putCountry("USA");
        traits.putAddress(address);
        traits.put("boolean", new Boolean(true));
        traits.put("integer", new Integer(50));
        traits.put("double", new Double(7.2));
        traits.put("float", new Float(120.4));
        traits.put("long", new Long(1234L));
        traits.put("string", "hello");
        traits.put("date", new Date(System.currentTimeMillis()));
        Analytics.with(getContext()).identify(newUser, traits, null);
      }
    });

    Button flushButton = view.findViewById(R.id.flushButton);
    flushButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Toast.makeText(getContext(), "flush() called.", Toast.LENGTH_LONG).show();
        Analytics.with(getContext()).flush();
      }
    });

    Button trackButton = view.findViewById(R.id.trackButton);
    trackButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        EditText customEventTextField =  view.findViewById(R.id.customEventField);
        EditText propertyKeyTextField = view.findViewById(R.id.propertyKeyField);
        EditText propertyValueTextField = view.findViewById(R.id.propertyValueField);
        String customEvent = "custom_event";
        String propertyKey = "property_key";
        String propertyValue = "property_value";
        if (customEventTextField.getText().toString().length() > 0) {
          customEvent = customEventTextField.getText().toString();
        }
        if (propertyKeyTextField.getText().toString().length() > 0) {
          propertyKey = propertyKeyTextField.getText().toString();
        }
        if (propertyValueTextField.getText().toString().length() > 0) {
          propertyValue = propertyValueTextField.getText().toString();
        }
        Toast.makeText(getContext(), "track() called for custom event '" + customEvent + "' and purchase 'custom_purchase'.", Toast.LENGTH_LONG).show();
        Analytics.with(getContext()).track(customEvent, new Properties().putValue(propertyKey, propertyValue));
        Properties purchaseProperties = new Properties();
        purchaseProperties.put("property_key", "property_value");
        purchaseProperties.putRevenue(10.0);
        purchaseProperties.putCurrency("JPY");
        Analytics.with(getContext()).track("custom_purchase", purchaseProperties);
        Analytics.with(getContext()).track("Install Attributed", new Properties()
            .putValue("provider", "Tune/Kochava/Branch")
            .putValue("campaign", new Properties()
                .putValue("source", "Network/FB/AdWords/MoPub/Source")
                .putValue("name", "Campaign Name")
                .putValue("content", "Organic Content Title")
                .putValue("ad_creative", "Red Hello World Ad")
                .putValue("ad_group", "Red Ones")));

        // Log multiple products
        Properties.Product[] products = new Properties.Product[2];
        products[0] = new Properties.Product("id1", "sku1", 1.00d);
        products[1] = new Properties.Product("id2", "sku2", 2.00d);
        Analytics.with(getContext()).track("another_purchase", new Properties().putProducts(products));
      }
    });
    return view;
  }
}
