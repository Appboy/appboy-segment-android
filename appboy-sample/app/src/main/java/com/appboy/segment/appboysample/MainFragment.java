package com.appboy.segment.appboysample;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.EditText;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.segment.analytics.Analytics;
import com.segment.analytics.Properties;
import com.segment.analytics.Traits;

import java.util.Date;
import java.util.Random;
import com.braze.Braze;

public class MainFragment extends Fragment {

  Random mRandom = new Random();

  public MainFragment() {}

  @Override
  public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState) {
    final View view = layoutInflater.inflate(R.layout.content_main, container, false);

    Button identifyButton = view.findViewById(R.id.identifyButton);
    identifyButton.setOnClickListener(new View.OnClickListener() {
      @SuppressLint("SetTextI18n")
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
        traits.put("boolean", Boolean.TRUE);
        traits.put("integer", 50);
        traits.put("double", 7.2);
        traits.put("float", 120.4f);
        traits.put("long", 1234L);
        traits.put("string", "hello");
        traits.put("string_array", new String[]{"one", "two", "foo"});
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
        Properties purchaseProperties = new Properties()
          .putRevenue(10.0)
          .putCurrency("JPY");
        purchaseProperties.put("property_key", "property_value");
        Analytics.with(getContext()).track("custom_purchase", purchaseProperties);
        Analytics.with(getContext()).track("Order Completed");
        Analytics.with(getContext()).track("Install Attributed", new Properties()
            .putValue("provider", "Tune/Kochava/Branch")
            .putValue("campaign", new Properties()
                .putValue("source", "Network/FB/AdWords/MoPub/Source")
                .putValue("name", "Campaign Name")
                .putValue("content", "Organic Content Title")
                .putValue("ad_creative", "Red Hello World Ad")
                .putValue("ad_group", "Red Ones")));

        // Log multiple products
        // You should see id1, id2 appear as purchases
        Properties.Product[] products = new Properties.Product[2];
        products[0] = new Properties.Product("id1", "sku1", 1.0);
        products[1] = new Properties.Product("id2", "sku2", 2.0);
        Analytics.with(getContext()).track("Order Completed", new Properties().putProducts(products));

        // You should see id3, id4 appear as purchases
        Properties.Product[] products2 = new Properties.Product[2];
        products2[0] = new Properties.Product("id3", "sku3", 1.0);
        products2[1] = new Properties.Product("id4", "sku4", 2.0);
        Analytics.with(getContext()).track("revenueEvent", new Properties().putProducts(products2).putRevenue(1.0));

        // You should see nonRevenueEvent appear as a custom event
        Properties.Product[] products3 = new Properties.Product[2];
        products3[0] = new Properties.Product("id5", "sku3", 1.0);
        products3[1] = new Properties.Product("id6", "sku4", 2.0);
        Analytics.with(getContext()).track("nonRevenueEvent", new Properties().putProducts(products3));
      }
    });

    Button disableSdkButton = view.findViewById(R.id.disableSdk);
    disableSdkButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Toast.makeText(getContext(), "disableSdk() called.", Toast.LENGTH_LONG).show();
        Braze.disableSdk(getContext());
        Braze.wipeData(getContext());
      }
    });

    Button enableSdkButton = view.findViewById(R.id.enableSdk);
    enableSdkButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View arg0) {
        Toast.makeText(getContext(), "enableSdk() called.", Toast.LENGTH_LONG).show();
        Braze.enableSdk(getContext());
      }
    });

    return view;
  }
}
