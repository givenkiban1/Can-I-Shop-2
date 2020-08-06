package com.example.canishop;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.MenuItem;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;

import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.UUID;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import de.hdodenhof.circleimageview.CircleImageView;

public class Home extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private static final String TAG = "Home";
    private static boolean doubleBackToExitPressedOnce = false;
    private StorageReference mStorageRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_home);

        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.
                        | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        // Hide the nav bar and status bar
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final DrawerLayout drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        navigationView.setNavigationItemSelectedListener(this);

        mAuth = FirebaseAuth.getInstance();
        mStorageRef = FirebaseStorage.getInstance().getReference();

        View headerView = navigationView.getHeaderView(0);
        TextView displayEmail = (TextView) headerView.findViewById(R.id.displayEmail);
        final TextView displayUserName = (TextView) headerView.findViewById(R.id.displayUserName);


        final FirebaseUser currentUser = mAuth.getCurrentUser();
        displayEmail.setText(currentUser.isEmailVerified()? currentUser.getEmail() : currentUser.getEmail()+" (email not verified)");

        final ConstraintLayout prof = drawer.findViewById(R.id.view_profile);

        final CircleImageView profile_pic = prof.findViewById(R.id.profile_image);

        Button btnPassChange = prof.findViewById(R.id.btnChangePass);

        btnPassChange.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mAuth.sendPasswordResetEmail(currentUser.getEmail()).addOnCompleteListener(
                        new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()){
                                    Toast.makeText(Home.this, "An email has been sent to you so that you can reset your password.", Toast.LENGTH_SHORT).show();
                                }
                                else{
                                    Toast.makeText(Home.this, "An error has occured.", Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                );


            }
        });

        if (!currentUser.isEmailVerified()){
            Toast.makeText(this, "Please verify your email address, an email was sent to you already!", Toast.LENGTH_LONG).show();
        }



        profile_pic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AlertDialog.Builder builder = new AlertDialog.Builder(Home.this);
                builder.setMessage("Do you want to change your profile picture ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // FIRE ZE MISSILES!
                                //User wants to change picture...
                                //pull up file upload dialog...

                                Intent pickPhoto = new Intent(Intent.ACTION_PICK,
                                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                                startActivityForResult(pickPhoto , 1);//one can be replaced with any action code

                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                // User cancelled the dialog
                            }
                        });
                // Create the AlertDialog object and return it
                builder.create();
                builder.show();


            }
        });


        //mAuth.sendPasswordResetEmail()
        // mAuth.confirmPasswordReset()
        //mAuth.verifyPasswordResetCode()

        // Write a message to the database
        FirebaseDatabase database = FirebaseDatabase.getInstance();
        DatabaseReference myRef = database.getReference("users").child(mAuth.getUid());

        // Read from the database
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String name = dataSnapshot.child("name").getValue(String.class);
                String sname = dataSnapshot.child("surname").getValue(String.class);
                String email = dataSnapshot.child("email").getValue(String.class);
                String uid = dataSnapshot.child("uniqueId").getValue(String.class);
                String utype = dataSnapshot.child("userType").getValue(String.class);
                String company = dataSnapshot.child("company").getValue(String.class);
                String profile = dataSnapshot.child("profilePic").getValue(String.class);

                if (profile!="default" && profile!=null){
                    Picasso.get().load(profile).into(profile_pic);
                }

                displayUserName.setText(name+" "+sname);



                TextView txtName = prof.findViewById(R.id.txtProfileNameSurname);
                txtName.setText(name+" "+sname);

                TextView txtEmail = prof.findViewById(R.id.txtProfileEmail);
                txtEmail.setText( currentUser.isEmailVerified()? email : email+" (email not verified)");

                TextView txtUType = prof.findViewById(R.id.txtProfileUserType);
                TextView txtCompany = prof.findViewById(R.id.txtProfileCompany);


                if (utype.equalsIgnoreCase("admin")){
                    //if user isn't normal user (nu), then they're either admin or employeee... so definitely have "company field"
                    txtUType.setText("User Type : "+utype);
                    txtCompany.setText("Works at : "+company);
                }
                else{
                    txtUType.setVisibility(View.GONE);
                    txtCompany.setVisibility(View.GONE);
                }





                Log.d(TAG, "Value is: " + name);
                Log.d(TAG, "Value is: " + sname);
                Log.d(TAG, "Value is: " + email);
                Log.d(TAG, "Value is: " + uid);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException());
            }
        });

    }

    //the below method is for when a user has picked a picture to upload...
    protected void onActivityResult(int requestCode, int resultCode, Intent imageReturnedIntent) {
        super.onActivityResult(requestCode, resultCode, imageReturnedIntent);
        switch(requestCode) {

            case 1:
                if(resultCode == RESULT_OK){

                    final DrawerLayout drawer = findViewById(R.id.drawer_layout);

                    final ConstraintLayout prof = drawer.findViewById(R.id.view_profile);

                    final CircleImageView profile_pic = prof.findViewById(R.id.profile_image);

                    Uri selectedImage = imageReturnedIntent.getData();


                    //update in database...
                    //upload to firebase storage

                    final StorageReference riversRef = mStorageRef.child("images");

                    riversRef.putFile(selectedImage)
                            .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                    // Get a URL to the uploaded content

                                    riversRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                        @Override
                                        public void onSuccess(Uri uri) {

                                            Uri downloadUrl = uri;

                                            Picasso.get().load(downloadUrl.toString()).into(profile_pic);

                                            FirebaseDatabase database = FirebaseDatabase.getInstance();
                                            DatabaseReference myRef = database.getReference("users");

                                            myRef.child(mAuth.getUid()).child("profilePic").setValue(downloadUrl.toString());

                                            Toast.makeText(Home.this, "Your profile picture has been successfully changed!", Toast.LENGTH_SHORT).show();


                                        }
                                    });


                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception exception) {
                                    // Handle unsuccessful uploads
                                    // ...

                                    Toast.makeText(Home.this, "There was an error uploading your profile image, please try again", Toast.LENGTH_SHORT).show();

                                }
                            });


                }
                break;
        }
    }




    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
        //updateUI(currentUser);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            DrawerLayout drawer = findViewById(R.id.drawer_layout);

            ConstraintLayout qr = drawer.findViewById(R.id.view_qr);
            qr.setVisibility(View.GONE);

            ConstraintLayout prof = drawer.findViewById(R.id.view_profile);
            prof.setVisibility(View.GONE);


            ConstraintLayout home = drawer.findViewById(R.id.view_home);
            home.setVisibility(View.VISIBLE);

            setTitle("Home");


        } else if (id == R.id.nav_logout) {

            //logout button
            mAuth.signOut();
            Toast.makeText(getApplicationContext(), "You have signed out successfully.",
                    Toast.LENGTH_SHORT).show();
            startActivity( new Intent(getApplicationContext() , Login.class) );
            finish();


        } else if (id == R.id.show_qr) {

            //load a new page where i'll show qr code...

            DrawerLayout drawer = findViewById(R.id.drawer_layout);

            ConstraintLayout qr = drawer.findViewById(R.id.view_qr);
            qr.setVisibility(View.VISIBLE);

            ConstraintLayout prof = drawer.findViewById(R.id.view_profile);
            prof.setVisibility(View.GONE);

            ConstraintLayout home = drawer.findViewById(R.id.view_home);
            home.setVisibility(View.GONE);

            String uniqueID = mAuth.getUid(); //this is what we store for user id and use to create qr code

            StringBuilder textToSend = new StringBuilder();
            textToSend.append(uniqueID);
            MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
            ImageView im = qr.findViewById(R.id.imageView4);

            try {
                BitMatrix bitMatrix = multiFormatWriter.encode(textToSend.toString(), BarcodeFormat.QR_CODE, 600, 600);
                BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
                Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
                im.setImageBitmap(bitmap);
                im.setVisibility(View.VISIBLE);

            } catch (WriterException e) {
                e.printStackTrace();
                Toast.makeText(this, "Error, image couldnt be generated", Toast.LENGTH_LONG).show();
                im.setBackgroundResource(R.drawable.tropical_beach_18);
            }


            setTitle("QR Code");

        } else if (id == R.id.nav_profile) {

            DrawerLayout drawer = findViewById(R.id.drawer_layout);

            ConstraintLayout qr = drawer.findViewById(R.id.view_qr);
            qr.setVisibility(View.GONE);

            ConstraintLayout prof = drawer.findViewById(R.id.view_profile);
            prof.setVisibility(View.VISIBLE);


            ConstraintLayout home = drawer.findViewById(R.id.view_home);
            home.setVisibility(View.GONE);

            setTitle("Profile");

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else

        {
            if (doubleBackToExitPressedOnce) {
                super.onBackPressed();
                return;
            }

            this.doubleBackToExitPressedOnce = true;
            Toast.makeText(this, "Please press BACK again to exit", Toast.LENGTH_SHORT).show();

            new Handler().postDelayed(new Runnable() {

                @Override
                public void run() {
                    doubleBackToExitPressedOnce=false;
                }
            }, 2000);
        }
    }

}
