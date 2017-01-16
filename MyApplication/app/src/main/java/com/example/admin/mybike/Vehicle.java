package com.example.admin.mybike;


import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import static android.R.id.list;

public class Vehicle extends AppCompatActivity {

    private static final int DELETE = 1;

    EditText nameTxt, distanceTxt,dateTxt, addressTxt;
    ImageView IV_GalleryPhotoPicker;
    List<VehicleHandler> Vehicle = new ArrayList<VehicleHandler>();
    ListView contactListView;
    VehicleDatabase dbVehicle;
    int longClickedItemIndex;
    ArrayAdapter<VehicleHandler> contactAdapter;
    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vehicle);
        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        nameTxt = (EditText) findViewById(R.id.txtName);
        distanceTxt = (EditText) findViewById(R.id.txtDistance);
        dateTxt = (EditText) findViewById(R.id.txtDate);
        addressTxt = (EditText) findViewById(R.id.txtAddress);
        contactListView = (ListView) findViewById(R.id.listView);
        IV_GalleryPhotoPicker = (ImageView) findViewById(R.id.ImageView_GalleryPhotoPicker);
        dbVehicle = new VehicleDatabase(getApplicationContext());
        registerForContextMenu(contactListView);

        contactListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                longClickedItemIndex = position;
                return false;
            }
        });
        /**============================== TAB NAVIGATION ===========================================*/
        TabHost tabHost = (TabHost) findViewById(R.id.tabHost);
        tabHost.setup();
        TabHost.TabSpec tabSpec = tabHost.newTabSpec("creator");
        tabSpec.setContent(R.id.tabCreator);
        tabSpec.setIndicator("Vehicle Creator");
        tabHost.addTab(tabSpec);
        tabSpec = tabHost.newTabSpec("list");
        tabSpec.setContent(R.id.tabContactList);
        tabSpec.setIndicator("My Vehicle");
        tabHost.addTab(tabSpec);

        final Button addBtn = (Button) findViewById(R.id.btnAdd);
        addBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                VehicleHandler contact = new VehicleHandler(dbVehicle.getContactsCount(), String.valueOf(nameTxt.getText()), String.valueOf(distanceTxt.getText()), String.valueOf(dateTxt.getText()), String.valueOf(addressTxt.getText()), imageViewtoByte(IV_GalleryPhotoPicker));
                if (!contactExists(contact)) {
                    dbVehicle.createContact(contact);
                    Vehicle.add(contact);
                    contactAdapter.notifyDataSetChanged();
                    Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) + " has been added!", Toast.LENGTH_SHORT).show();
                    return;
                }
                Toast.makeText(getApplicationContext(), String.valueOf(nameTxt.getText()) + " already exists. Please use a different name.", Toast.LENGTH_SHORT).show();

            }
        });



        nameTxt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
                addBtn.setEnabled(String.valueOf(nameTxt.getText()).trim().length() > 0);
        }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
//       =========================== Image View (button) ==============================================
        IV_GalleryPhotoPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), 1);
            }
            });

        if (dbVehicle.getContactsCount() != 0){
            Vehicle.addAll(dbVehicle.getAllContacts());
        }

        populateList();

    }

    private byte[] imageViewtoByte(ImageView IMV) {
        Bitmap bitmap = ((BitmapDrawable)IMV.getDrawable()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        bitmap.compress(Bitmap.CompressFormat.PNG,100,stream);
        byte[] byteArray = stream.toByteArray();
        return  byteArray;

    }



    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, view, menuInfo);

        menu.setHeaderIcon(R.drawable.pencil_icon);
        menu.setHeaderTitle("List Options");

        menu.add(Menu.NONE, DELETE, menu.NONE, "Delete");
    }

    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case DELETE:
                dbVehicle.deleteContact(Vehicle.get(longClickedItemIndex));
                Vehicle.remove(longClickedItemIndex);
                contactAdapter.notifyDataSetChanged();
                break;
        }

        return super.onContextItemSelected(item);
    }

    private boolean contactExists(VehicleHandler contact) {
        String name = contact.getName();
        int contactCount = Vehicle.size();

        for (int i = 0; i < contactCount; i++) {
            if (name.compareToIgnoreCase(Vehicle.get(i).getName()) == 0)
                return true;
        }
        return false;
    }

    public void onActivityResult(int reqCode, int resCode, Intent data) {
        if (resCode == RESULT_OK) {
            if (reqCode == 1) {
               imageUri = data.getData();
                try{
                    InputStream inputStream = getContentResolver().openInputStream(imageUri);

                    Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                    IV_GalleryPhotoPicker.setImageBitmap(bitmap);
                }catch (Exception e){

            }
        }
    }
    }

    private void populateList() {
        contactAdapter = new ContactListAdapter();
        contactListView.setAdapter(contactAdapter);
    }


    /**==============================LIST==================*/
    private class ContactListAdapter extends ArrayAdapter<VehicleHandler> {
        public ContactListAdapter() {
            super (Vehicle.this, R.layout.listview_vehicle, Vehicle);
        }

        @Override
        public View getView(int position, View view, ViewGroup parent) {
            if (view == null)
                view = getLayoutInflater().inflate(R.layout.listview_vehicle, parent, false);
            VehicleHandler currentVehicle = Vehicle.get(position);
            TextView name = (TextView) view.findViewById(R.id.txtVehicleName);
            name.setText(currentVehicle.getName());
            TextView distance = (TextView) view.findViewById(R.id.distanceNumber);
            distance.setText(currentVehicle.getDistance());
            TextView date = (TextView) view.findViewById(R.id.Date);
            date.setText(currentVehicle.getDate());
            TextView address = (TextView) view.findViewById(R.id.Address);
            address.setText(currentVehicle.getAddress());

            byte[] vehicleimage = currentVehicle.getImageURI();
            Bitmap bitmap = BitmapFactory.decodeByteArray(vehicleimage, 0, vehicleimage.length);

            ImageView ivContactImage = (ImageView) view.findViewById(R.id.ivContactImage);
            ivContactImage.setImageBitmap(bitmap);
            return view;
        }


    }



}
