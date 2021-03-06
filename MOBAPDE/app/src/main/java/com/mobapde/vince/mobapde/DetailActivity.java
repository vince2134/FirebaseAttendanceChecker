package com.mobapde.vince.mobapde;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import de.hdodenhof.circleimageview.CircleImageView;

public class DetailActivity extends AppCompatActivity {

    private static final String BUNDLE_EXTRAS = "BUNDLE_EXTRAS";
    private static final String EXTRA_QUOTE = "EXTRA_QUOTE";
    private static final String EXTRA_ATTR = "EXTRA_ATTR";
    public static final String DONE_ATTENDANCE = "DONE_ATTENDANCE";

    CircleImageView facultyImage;
    TextView facultyName, facultyCourse, roomName, classTime;
    Attendance item;

    Button submitButton, ab, ed, la, pr, sb, sw, us, vr;
    ImageButton saveBtn;
    String currentCode;
    EditText remark;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.view_item_v3);

        Toolbar toolbar = (Toolbar) findViewById(R.id.detailToolbar);
        toolbar.setTitle("Class Details");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        facultyImage = (CircleImageView) findViewById(R.id.facultyImage);
        facultyName = (TextView) findViewById(R.id.facultyName);
        facultyCourse = (TextView) findViewById(R.id.facultyCourse);
        //roomName = (TextView) findViewById(R.id.facultyRoom);
        classTime = (TextView) findViewById(R.id.classTime);
        submitButton = (Button) findViewById(R.id.submitButton);
        remark = (EditText) findViewById(R.id.remarkField);

        item = (Attendance) getIntent().getSerializableExtra(Attendance.ATTENDANCE);


        facultyName.setText(item.getFacultyName());
        facultyCourse.setText(item.getCourseCode());

        //Log.d("COURSECODE", item.getCourseCode());
        toolbar.setTitle(item.getRoom());
        //roomName.setText(item.getRoom());
        classTime.setText(formatClassTime(item));
        if(item.getPic() != null)
            Picasso.with(DetailActivity.this).load(item.getPic()).into(facultyImage);

        ab = (Button) findViewById(R.id.abBtn);
        ed = (Button) findViewById(R.id.edBtn);
        la = (Button) findViewById(R.id.laBtn);
        pr = (Button) findViewById(R.id.prBtn);
        sb = (Button) findViewById(R.id.sbBtn);
        sw = (Button) findViewById(R.id.swBtn);
        us = (Button) findViewById(R.id.usBtn);
        vr = (Button) findViewById(R.id.vrBtn);

        if(MainActivity.submitted){
            disableButtons();

            if(item.getRemarks().length() == 0)
                remark.setVisibility(View.GONE);
            else {
                remark.setEnabled(false);
            }

            submitButton.setVisibility(View.GONE);
        }

        setListeners();
        setSelectedCode(item.getCode());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == R.id.action_save){
            if (currentCode != null){
                this.item.setCode(currentCode);
                this.item.setRemarks(remark.getText().toString());
                Intent intent = new Intent();
                intent.putExtra(DONE_ATTENDANCE, this.item);
                setResult(RESULT_OK, intent);
                Toast.makeText(getBaseContext(), "Attendance successfully saved.", Toast.LENGTH_SHORT).show();
                finish();
            }
            else
                Toast.makeText(getBaseContext(), "Please select a status.", Toast.LENGTH_SHORT).show();
        }

        return super.onOptionsItemSelected(item);
    }

    public String formatClassTime(Attendance item){
        String timeStringFormat = "";
        Long startTime = item.getStartTime();
        Long endTime = item.getEndTime();

        String timeFormat = "HH:mm";
        SimpleDateFormat formatter = new SimpleDateFormat(timeFormat);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startTime);

        timeStringFormat += formatter.format(calendar.getTime());
        int h = calendar.get(Calendar.HOUR_OF_DAY);
        int m = calendar.get(Calendar.MINUTE);


        Log.i("time", "start "+ h + ":" + m);

        calendar.setTimeInMillis(endTime);

        h = calendar.get(Calendar.HOUR_OF_DAY);
        m = calendar.get(Calendar.MINUTE);

        Log.i("time", "end "+ h + ":" + m);

        timeStringFormat += " - " + formatter.format(calendar.getTime());



        return timeStringFormat;
    }

    public void disableButtons(){
        ab.setEnabled(false);
        ed.setEnabled(false);
        la.setEnabled(false);
        pr.setEnabled(false);
        pr.setEnabled(false);
        sb.setEnabled(false);
        sw.setEnabled(false);
        us.setEnabled(false);
        vr.setEnabled(false);
    }

    public void setListeners(){
        ab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = ab.getText().toString();
                ab.setTextColor(Color.WHITE);
                ab.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        ed.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = ed.getText().toString();
                ed.setTextColor(Color.WHITE);
                ed.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        la.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = la.getText().toString();
                la.setTextColor(Color.WHITE);
                la.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        pr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = pr.getText().toString();
                pr.setTextColor(Color.WHITE);
                pr.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        sb.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = sb.getText().toString();
                sb.setTextColor(Color.WHITE);
                sb.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        sw.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = sw.getText().toString();
                sw.setTextColor(Color.WHITE);
                sw.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        us.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = us.getText().toString();
                us.setTextColor(Color.WHITE);
                us.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                vr.setTextColor(Color.BLACK);
                vr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        vr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentCode = vr.getText().toString();
                vr.setTextColor(Color.WHITE);
                vr.getBackground().setColorFilter(Color.rgb(27, 32, 35), PorterDuff.Mode.MULTIPLY);
                ed.setTextColor(Color.BLACK);
                ed.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                la.setTextColor(Color.BLACK);
                la.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                pr.setTextColor(Color.BLACK);
                pr.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sb.setTextColor(Color.BLACK);
                sb.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                sw.setTextColor(Color.BLACK);
                sw.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                us.setTextColor(Color.BLACK);
                us.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                ab.setTextColor(Color.BLACK);
                ab.getBackground().setColorFilter(Color.WHITE, PorterDuff.Mode.MULTIPLY);
                //submitButton.setBackgroundColor(Color.rgb(8, 120, 48));
                //submitButton.setTextColor(Color.WHITE);
                submitButton.setEnabled(true);
            }
        });
        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentCode != null){
                    item.setCode(currentCode);
                    item.setRemarks(remark.getText().toString());
                    Intent intent = new Intent();
                    intent.putExtra(DONE_ATTENDANCE, item);
                    setResult(RESULT_OK, intent);
                    Toast.makeText(getBaseContext(), "Attendance successfully saved.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        });

        //submitButton.setBackgroundColor(Color.GRAY);
        //submitButton.setTextColor(Color.WHITE);
        submitButton.setEnabled(false);
    }

    private void setSelectedCode(String code){
        remark.setText(item.getRemarks());

        if(code != null){
            if(code.equals("Absent")){
                ab.performClick();
            }
            else if(code.equals("Early Dismissal")){
                ed.performClick();
            }
            else if(code.equals("Late")){
                la.performClick();
            }
            else if(code.equals("Present")){
                pr.performClick();
            }
            else if(code.equals("Seatwork")){
                sw.performClick();
            }
            else if(code.equals("Substitute")){
                sb.performClick();
            }
            else if(code.equals("Unscheduled")){
                us.performClick();
            }
            else if(code.equals("Vacant Room")){
                vr.performClick();
            }
        }
    }
}
