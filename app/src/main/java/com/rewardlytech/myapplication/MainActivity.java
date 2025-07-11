package com.rewardlytech.myapplication;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.accounts.Account;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.DialogFragment;

//clover
import com.clover.sdk.util.CloverAccount;
import com.clover.sdk.v1.ClientException;
import com.clover.sdk.v1.ServiceException;
import com.clover.sdk.v3.inventory.InventoryConnector;
import com.clover.sdk.v3.inventory.InventoryContract;
import com.clover.sdk.v3.inventory.Item;
import com.clover.sdk.v1.BindingException;

public class MainActivity extends AppCompatActivity {


    private Account rAccount;
    private InventoryConnector rConnector;
    private TextView rTextview;
    private Button rMemberBtn;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        rTextview = (TextView) findViewById(R.id.tv_maintext);
        rMemberBtn = (Button) findViewById(R.id.btn_members);
        rMemberBtn.setText("Members");
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Add Members")
                .setPositiveButton("Scan", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Start barcode Scanner.
                        Toast.makeText(MainActivity.this.getApplicationContext(), "Scan started", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //exit

                    }
                });
        AlertDialog membAdd = builder.create();
        rMemberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                membAdd.setMessage("Member add");
                membAdd.show();
            }
        });
        if (rAccount == null) {
            rAccount = CloverAccount.getAccount(this);

            if (rAccount == null) {
                return;
            }
        }

        //connectinventory
        connect();

        new InventoryAsyncTask().execute();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        disconnect();
    }

    private void connect() {
        disconnect();

        if (rAccount != null) {
            rConnector = new InventoryConnector(this, rAccount, null);
            rConnector.connect();
        }
    }

    private void disconnect() {
        if (rConnector != null) {
            rConnector.disconnect();
            rConnector = null;
        }
    }

    private class InventoryAsyncTask extends AsyncTask<Void, Void, Item> {
        @Override
        protected final Item doInBackground(Void... unused) {
            String itemId = null;
            try (Cursor cursor = getContentResolver().query(InventoryContract.Item.contentUriWithAccount(rAccount),
                    new String[]{InventoryContract.Item.UUID}, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    itemId = cursor.getString(0);
                }
                return itemId != null ? rConnector.getItem(itemId) : null;
            } catch (RemoteException | ClientException | ServiceException | BindingException e) {
                e.printStackTrace();
            }
            return null;

        }


        @Override
        protected final void onPostExecute(Item item) {
            if (item != null) {
                rTextview.setText(item.getName());
            }
        }
    }


}
