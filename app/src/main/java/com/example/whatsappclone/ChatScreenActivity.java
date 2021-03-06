package com.example.whatsappclone;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.google.firebase.storage.network.ListNetworkRequest;
import com.squareup.picasso.Picasso;

import java.sql.Time;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

import de.hdodenhof.circleimageview.CircleImageView;
public class ChatScreenActivity extends AppCompatActivity {
    private Button back;
    private FloatingActionButton send, imageButton;
    private EditText text;
    private String message;
    private String name, url, friendId, myId, time;
    private int SELECT_PICTURE = 200;
    private int CAPTURE_IMAGE = 100;
    private TextView textView_name;
    private CircleImageView profilepic;
    private FirebaseUser user;

    private List<ChatsModel> chatsList;
    private ChatsAdapter adapter;
    private RecyclerView recView;
    private ProgressBar progressBar;
    private int imageCount = 0;
    private String date;
    private int opp;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.in_chat_screen);

//        getSupportActionBar().hide();

        opp = 0;

        user = FirebaseAuth.getInstance().getCurrentUser();

        back = findViewById(R.id.back_button);
        send = findViewById(R.id.sendButton);
        text = findViewById(R.id.text);
        textView_name = findViewById(R.id.name);
        profilepic = findViewById(R.id.profilepic);
        imageButton = findViewById(R.id.imageButton);
        progressBar = findViewById(R.id.progress_bar_chat);

        imageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder alert = new AlertDialog.Builder(ChatScreenActivity.this);
                View view = getLayoutInflater().inflate(R.layout.image_option_dialog, null);
                Button camera = view.findViewById(R.id.camera_button);
                Button gallery = view.findViewById(R.id.gallery_button);
                Button dismiss = view.findViewById(R.id.dismiss_button);
                alert.setView(view);
                final AlertDialog alertDialog = alert.create();
                alertDialog.setCanceledOnTouchOutside(false);
                alertDialog.show();

                camera.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        //Request for camera runtime permission
                        if (ContextCompat.checkSelfPermission(ChatScreenActivity.this, Manifest.permission.CAMERA)
                                != PackageManager.PERMISSION_GRANTED) {
                            ActivityCompat.requestPermissions(ChatScreenActivity.this, new String[]{
                                    Manifest.permission.CAMERA
                            }, 100);
                        }

                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(intent, 100);

                        alertDialog.dismiss();
                    }
                });

                gallery.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        imageChooser();
                        alertDialog.dismiss();
                    }
                });

                dismiss.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alertDialog.dismiss();
                    }
                });


            }
        });

        recView = findViewById(R.id.chats_recView);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setStackFromEnd(true);
        recView.setLayoutManager(linearLayoutManager);


        Intent intent = getIntent();
        Bundle b = intent.getExtras();
        name = (String) b.get("name");
        url = (String) b.get("url");
        friendId = (String) b.get("friendId");
        System.out.println("Friend Id : " + friendId);
        myId = user.getUid();

        textView_name.setText(name);

        if (url.equals("default")) {
            String uri = "@drawable/profilepicc";
            int imageResource = getResources().getIdentifier(uri, null, getPackageName());
            Drawable res = getResources().getDrawable(imageResource);
            profilepic.setImageDrawable(res);
        } else {
            Picasso.with(ChatScreenActivity.this).load(url).into(profilepic);
        }

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                opp = 1;
                Intent intent1 = new Intent(ChatScreenActivity.this, MainActivity.class);
                startActivity(intent1);
                finish();
            }
        });
        message = text.getText().toString();
        System.out.println(message);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Send text
                message = text.getText().toString();
                sendMessage(myId, friendId, message, "null");
                System.out.println(message);
                text.setText("");
            }
        });
        send.setEnabled(false);
        text.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (s.toString().trim().length() > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.toString().trim().length() > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().trim().length() > 0) {
                    send.setEnabled(true);
                } else {
                    send.setEnabled(false);
                }
            }
        });

        readMessages(myId, friendId);
    }

    private void readMessages(String myId, String friendId) {
        chatsList = new ArrayList<>();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chatsList.clear();

                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    ChatsModel chats = snapshot1.getValue(ChatsModel.class);
                    if (chats.getSender().equals(myId) && chats.getReceiver().equals(friendId) || chats.getSender().equals(friendId) && chats.getReceiver().equals(myId)) {
                        chatsList.add(chats);
                    }

                    adapter = new ChatsAdapter(ChatScreenActivity.this, chatsList);
                    recView.setAdapter(adapter);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void sendMessage(String myId, String friendId, String message, String uri) {
        time = new SimpleDateFormat("HH:mm:ss").format(Calendar.getInstance().getTime());
        date = new SimpleDateFormat("dd/MM/yyyy").format(Calendar.getInstance().getTime());
        final int[] flag = {0};
        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Time");
        reference1.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                    TimeModel timeModel = snapshot1.getValue(TimeModel.class);
                    if (timeModel.getMyId().equals(myId) && timeModel.getFriendId().equals(friendId)) {
                        flag[0] = 1;
                        HashMap<String, Object> hashMap = new HashMap();
                        hashMap.put("date", date);
                        hashMap.put("time", time);
                        reference1.child(timeModel.getId()).updateChildren(hashMap);
                    }

                }
                if (flag[0] == 0) {
                    HashMap<String, Object> hashMap = new HashMap<>();
                    hashMap.put("myId", myId);
                    hashMap.put("friendId", friendId);
                    hashMap.put("time", time);
                    hashMap.put("date", date);
                    String mGroupId = reference1.push().getKey();
                    hashMap.put("id", mGroupId);
                    reference1.child(mGroupId).setValue(hashMap);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        System.out.println("ImageUrl : " + uri);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> hashMap = new HashMap<>();
        hashMap.put("sender", myId);
        hashMap.put("receiver", friendId);
        hashMap.put("message", message);
        hashMap.put("imageUrl", uri);
        hashMap.put("time", time);
        hashMap.put("date", date);
        databaseReference.child("Chats").push().setValue(hashMap);
    }

    void imageChooser() {

        // create an instance of the
        // intent of the type image
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(Intent.ACTION_GET_CONTENT);

        // pass the constant to compare it
        // with the returned requestCode
        startActivityForResult(Intent.createChooser(i, "Select Picture"), SELECT_PICTURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == CAPTURE_IMAGE) {
            final String[] imageUrl = new String[1];
            Button imagePreviewSend;
            ImageView previewImage;
            EditText image_text;
            final AlertDialog.Builder alert = new AlertDialog.Builder(ChatScreenActivity.this);
            View view = getLayoutInflater().inflate(R.layout.image_preview_dialog, null);
            imagePreviewSend = view.findViewById(R.id.preview_button);
            previewImage = view.findViewById(R.id.image_preview);
            image_text = view.findViewById(R.id.image_text);
            alert.setView(view);
            final AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();

            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            previewImage.setImageBitmap(bitmap);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            String path = MediaStore.Images.Media.insertImage(getContentResolver(), bitmap, "Title", null);
            Uri imageUri = Uri.parse(path);
            imageUrl[0] = imageUri.toString();

            imagePreviewSend.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String message = image_text.getText().toString().trim();
                    alertDialog.dismiss();
                    sendMessage(myId, friendId, message, imageUrl[0]);
                }
            });
        } else if (resultCode == RESULT_OK && requestCode == SELECT_PICTURE) {
            final String[] imageUrl = new String[1];
            Button imagePreviewSend;
            ImageView previewImage;
            EditText image_text;
            final AlertDialog.Builder alert = new AlertDialog.Builder(ChatScreenActivity.this);
            View view = getLayoutInflater().inflate(R.layout.image_preview_dialog, null);
            imagePreviewSend = view.findViewById(R.id.preview_button);
            previewImage = view.findViewById(R.id.image_preview);
            image_text = view.findViewById(R.id.image_text);
            alert.setView(view);
            final AlertDialog alertDialog = alert.create();
            alertDialog.setCanceledOnTouchOutside(false);
            alertDialog.show();
            // compare the resultCode with the
            // SELECT_PICTURE constant
            // Get the url of the image from data
            Uri selectedImageUri = data.getData();
            if (null != selectedImageUri) {
                // update the preview image in the layout
                progressBar.setVisibility(View.VISIBLE);
                Toast.makeText(ChatScreenActivity.this, "This may take a few seconds", Toast.LENGTH_LONG).show();
                imagePreviewSend.setEnabled(false);
                imagePreviewSend.getBackground().setAlpha(50);
                System.out.println(selectedImageUri.toString());
                while (selectedImageUri == null) {
                    continue;
                }
                //previewImage.setImageURI(selectedImageUri);
                Picasso.with(ChatScreenActivity.this).load(selectedImageUri.toString()).into(previewImage);

                String filepath = "Photos/" + "chatImages" + user.getUid() + String.valueOf(imageCount);
                imageCount++;
                StorageReference reference = FirebaseStorage.getInstance().getReference(filepath);
                if (selectedImageUri != null) {
                    // update the preview image in the layout
                    reference.putFile(selectedImageUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Task<Uri> task = taskSnapshot.getMetadata().getReference().getDownloadUrl();

                            task.addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    imageUrl[0] = uri.toString();
                                    System.out.println("Photo added in Storage");
                                    imagePreviewSend.setEnabled(true);
                                    imagePreviewSend.getBackground().setAlpha(255);
                                    progressBar.setVisibility(View.INVISIBLE);
                                }
                            });
                        }
                    });

                    imagePreviewSend.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            String message = image_text.getText().toString().trim();
                            alertDialog.dismiss();
                            sendMessage(myId, friendId, message, imageUrl[0]);
                        }
                    });
                }
            }
        }
    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent1 = new Intent(ChatScreenActivity.this, MainActivity.class);
        startActivity(intent1);
        finish();
    }

}