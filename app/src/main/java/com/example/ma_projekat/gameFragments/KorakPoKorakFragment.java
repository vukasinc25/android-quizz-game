package com.example.ma_projekat.gameFragments;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.CountDownTimer;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ma_projekat.GameActivity;
import com.example.ma_projekat.Model.Data;
import com.example.ma_projekat.Model.KorakPoKorak;
import com.example.ma_projekat.Model.UserDTO;
import com.example.ma_projekat.R;
import com.example.ma_projekat.Repository.UserRepository;
import com.example.ma_projekat.Utils.MqttHandler;
import com.example.ma_projekat.Utils.ShowHideElements;
import com.example.ma_projekat.Utils.TempGetData;
import com.example.ma_projekat.menuFragments.MyViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class KorakPoKorakFragment extends Fragment {
    private MyViewModel viewModel;
    View view;
    Dialog dialog;
    GameActivity gameActivity;
    TextView scoreTimer;
    CountDownTimer countDownTimer;
    AppCompatActivity activity;
    MqttHandler mqttHandler = new MqttHandler();
    TempGetData tempGetData = new TempGetData();
    TextView textView1, textView2, textView3, textView4, textView5, textView6, textView7, textView_answer, points_right, player1Score, player2Score,  player2UserName;
    Map<Integer, TextView> textViwMap = new HashMap<>();
    Map<String, Object> runda1 = new HashMap<>();
    ArrayList<String> arrayList = new ArrayList<>();
    int count = 2;
    int score = 0;
    int counter1 = 0;
    boolean isOkButtonClicked = false;
    String response = "";
    boolean isAnswerCorrect = false;
    boolean isFirstRound, isOnline, isMyTurn;
    private AsyncTask myTask;
    private static final String KEY_STATE = "state";
    UserRepository userRepository = new UserRepository();

    public KorakPoKorakFragment() {
    }

    AsocijacijeFragment associationsFragment = new AsocijacijeFragment();
    public void setIsMyTurn(){
        if(isMyTurn){
            isMyTurn = false;
        }else{
            isMyTurn = true;
        }
    }

    public static KorakPoKorakFragment newInstance(boolean round, boolean bool) {

        Bundle args = new Bundle();
        args.putBoolean("isFirstRound", round);
        args.putBoolean("isOnline", bool);

        KorakPoKorakFragment fragment = new KorakPoKorakFragment();
        fragment.setArguments(args);
        return fragment;
    }
    public void TempGetDataMethod(String runda){
        TempGetData.getKorakPoKorak(new TempGetData.FireStoreCallback() {
            @Override
            public void onCallBack(ArrayList<String> list) {
                arrayList.addAll(list);
                textView1.setText(arrayList.get(0));
                textView2.setText(arrayList.get(1));
                textView3.setText(arrayList.get(2));
                textView4.setText(arrayList.get(3));
                textView5.setText(arrayList.get(4));
                textView6.setText(arrayList.get(5));
                textView7.setText(arrayList.get(6));
                response = arrayList.get(7);
//                textView_answer.setText(arrayList.get(7));

                textViwMap.put(1, textView1);
                textViwMap.put(2, textView2);
                textViwMap.put(3, textView3);
                textViwMap.put(4, textView4);
                textViwMap.put(5, textView5);
                textViwMap.put(6, textView6);
                textViwMap.put(7, textView7);
                textViwMap.put(8, textView_answer);

                Log.d("LITS", arrayList.toString());

                if(getArguments() != null){
                    isOnline = getArguments().getBoolean("isOnline", false);
                }


                if(isOnline){
                    isMyTurn = mqttHandler.getTurnPlayer();
                    if (getArguments() != null) {
                        setIsMyTurn();
                    }
                }
            }
        }, runda);
    }

    public void setIsOnline(boolean bool){
        isOnline = true;
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_korak_po_korak, container, false);
        textView1 = view.findViewById(R.id.hint1);
        textView1.setTextColor(Color.RED);
        textView2 = view.findViewById(R.id.hint2);
        textView3 = view.findViewById(R.id.hint3);
        textView4 = view.findViewById(R.id.hint4);
        textView5 = view.findViewById(R.id.hint5);
        textView6 = view.findViewById(R.id.hint6);
        textView7 = view.findViewById(R.id.hint7);
        textView_answer = view.findViewById(R.id.korakpokorak_answer);
        player1Score = activity.findViewById(R.id.player1Score);
        player2Score = activity.findViewById(R.id.player2Score);

        isOnline = Data.isOnline;
        gameActivity = (GameActivity) getActivity();
        scoreTimer = gameActivity.findViewById(R.id.time);
        player2UserName = activity.findViewById(R.id.player2Name);



        //neki if sa savedInstanceState koji ce da kaze koja je runda
        if (getArguments() == null) {
            TempGetDataMethod("runda1");
        }else {
            TempGetDataMethod("runda2");
        }

        Button confirm =  view.findViewById(R.id.korakpokorak_confirm);
//        EditText answer = view.findViewById(R.id.korakpokorak_answer);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isOnline && !isMyTurn){
                    Toast.makeText(getActivity(),"Nije tvoj red", Toast.LENGTH_SHORT).show();

                }
                else {
                    String editText1 = textView_answer.getText().toString();
                    if (textView_answer.toString() != "") {
                        if (editText1.equals(response)) {
                            isOkButtonClicked = true;
                            countDownTimer.cancel();
                            isCorect();
                            Log.i("mqtt", "Pre nego sto se proveri da li je saveInstanceState == null, " + "saveInstanceState: " + savedInstanceState);
                            if (isOnline) {
                                if (getArguments() == null) {
                                    //                                    isOkButtonClicked = false;
                                    Log.i("mqtt", "IsFirstRound = true");
                                    mqttHandler.korakPoKorakPublish(textView_answer, true, false);
                                    isFragment(true);
                                    //                                    viewModel.setIsFirstRound(false);
                                    //                                } else if(isFirstRound == false && isOkButtonClicked) {
                                } else {
                                    //                            isOkButtonClicked = false;
                                    Log.i("mqtt", "IsFirstRound = false");
                                    mqttHandler.korakPoKorakPublish(textView_answer, false, false);
                                    isFragment(false);
                                    //                            viewModel.setIsFirstRound(true);
                                    //                                }
                                    //                            });
                                }
                            }
                            for (Map.Entry<Integer, TextView> entry : textViwMap.entrySet()) {
                                TextView textView = entry.getValue();
                                textView.setTextColor(Color.RED);
                            }
                        } else {
                            Toast.makeText(getActivity(), "Pokusaj ponovo", Toast.LENGTH_SHORT).show();
                        }
                    }
                }
//                dialog.dismiss();
            }
        });
        return view;
    }

    int x = 0;
    private void isCorect() {
        for (Map.Entry<Integer, TextView> entry : textViwMap.entrySet()) {
            if(entry.getKey().equals(8)){
                break;
            }
            if(entry.getValue().getCurrentTextColor() != Color.RED){
                x++;
            }
        }
        score = Integer.parseInt((String) player1Score.getText());
        if(x == 1){
            score += 10;
        }else if(x == 2){
            score += 12;
        }else if(x == 3){
            score += 14;
        }else if(x == 4){
            score += 16;
        }else if(x == 5){
            score += 18;
        }else if(x == 6){
            score += 20;
        }
        isAnswerCorrect = true;
        player1Score.setText(score + "");

        if(isOnline){
            Data.loggedInUser.setKorakPoKorak(Data.loggedInUser.getKorakPoKorak()+score);
            userRepository.updateKorakPoKorak(Data.loggedInUser, Data.loggedInUser.getKorakPoKorak()+score);
            mqttHandler.pointPublish(score);
        }
        if(!isOnline){
            getParentFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameLayout2, new AsocijacijeFragment())
                    .setReorderingAllowed(true)
                    .commit();
        }
    }

    public void isFragment(boolean bool) {
        if (isAdded()) {
            if(bool){
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout2, KorakPoKorakFragment.   newInstance(true, true))
                        .setReorderingAllowed(true)
                        .commit();
            }else{
                getParentFragmentManager()
                        .beginTransaction()
                        .replace(R.id.frameLayout2, new AsocijacijeFragment())
                        .setReorderingAllowed(true)
                        .commit();
            }
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MyViewModel.class);

        activity = (AppCompatActivity) getActivity();
//        player2UserName = activity.findViewById(R.id.player2Name)

        String p2name = "";
        if(isOnline){
            p2name = Data.user2.getUsername();
        }
        if(Data.loggedInUser != null && !p2name.equals("Guest")){
            mqttHandler.korakPoKorakSubscribe(new MqttHandler.KorakPoKorakCallback() {
                @Override
                public void onCallBack(KorakPoKorak korakPoKorak) {
                    if(korakPoKorak != null){
                        activity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Log.i("mqtt", "Loged User: "+Data.loggedInUser.getUsername());
//                                Log.i("mqtt", "IsFirstRound u subcribu: "+korakPoKorak.isFirstRound().toString());
                                if(korakPoKorak.isFirstRound() && !korakPoKorak.isSecondPlayerTurn()){
//                                    if(isAdded()){
                                    Log.i("mqtt", "Ako je poslat publis u tacnom odg i isFristRound je true");
                                    isFragment(true);
                                    Log.i("mqtt", "isFirstRound = true i zavrsio se FragmentMenager");
//                                    }
                                }else if(!korakPoKorak.isFirstRound() && !korakPoKorak.isSecondPlayerTurn()){
//                                    if(isAdded()){
                                    Log.i("mqtt", "Ako je poslat publis u tacnom odg i isFristRound je false");
                                    isFragment(false);
                                    Log.i("mqtt", "isFirstRound = false i zavrsio se FragmentMenager");
//                                    }
                                }
                                if(korakPoKorak.isSecondPlayerTurn()){
                                    Log.i("mqtt", "isMyTurn u subskrajbu pre nego sto je pomenjeno: " + isMyTurn);
                                    setIsMyTurn();
                                    Log.i("mqtt", "isMyTurn u subskrajbu posle nego sto je pomenjeno: " + isMyTurn);
                                    countDownTimer = new CountDownTimer(60000, 1000) {
                                        @Override
                                        public void onTick(long l) {
                                            Log.e("AA", "rad3" + l);
                                            Long min = ((l / 1000) % 3600) / 60;
                                            Long sec = (l / 1000) % 60;
                                            String format = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
                                            scoreTimer.setText(format);
                                        }

                                        @Override
                                        public void onFinish() {
                                            scoreTimer.setText("00:00");
                                            if(savedInstanceState == null){
                                                getParentFragmentManager().beginTransaction().replace(R.id.frameLayout2, new KorakPoKorakFragment().newInstance(true, true)).commit();
                                                isFirstRound = false;
                                            }else{
                                                associationsFragment.setIsOnline(true);
                                                getParentFragmentManager().beginTransaction().replace(R.id.frameLayout2, associationsFragment).commit();
                                            }
                                        }
                                    }.start();
                                }
                            }
                        });
                    }
                }
            });

            mqttHandler.pointSubscribe(new MqttHandler.PointCallback() {
                @Override
                public void onCallback(UserDTO userDTO) {
                    activity.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            player2Score.setText(userDTO.getPoints()+"");
                        }
                    });
                }
            });
        }

//        ShowHideElements.showScoreBoard(activity);

        countDownTimer = new CountDownTimer(70000, 1000) {
            @Override
            public void onTick(long l) {
                if (Math.abs(l % 10000) < 1200) {
                    Log.e("AA","radi1");
                    if(count < 9){
                        Log.e("AA","rad2");
                        //treba da se stavi if ako igra jedna ili dva igraca ako igra jedan igrac onda treba da se pokazuje resenje na kraju ako ne onda treba da bude prazno resenje
                        if(isOnline){
                            if(count == 8){

                            }
                            else{
                                TextView textView = (TextView)textViwMap.get(count);
                                textView.setTextColor(Color.RED);
                            }
                        }else{
                            TextView textView = (TextView)textViwMap.get(count);
                            textView.setTextColor(Color.RED);
                        }
                        count++;
                    }
                }
                Log.e("AA","rad3" + l);
                Long min = ((l / 1000) % 3600) / 60;
                Long sec = (l / 1000) % 60;
                String format = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
                if (scoreTimer != null) {
                    scoreTimer.setText(format);
                } else {
                    Log.e("YourTag", "scoreTimer is null");
                }

            }

            @Override
            public void onFinish() {
                scoreTimer.setText("00:00");
                count = 1;
                if(!isOnline){
                    getParentFragmentManager()
                            .beginTransaction()
                            .replace(R.id.frameLayout2, new AsocijacijeFragment())
                            .setReorderingAllowed(true)
                            .commit();
                }
                if(isOnline){
//                if(!isAnswerCorrect){ //ako je prva ili drugs igra i igrac nije pogodio resenje jos jedan timer za 1 min
                    Log.i("mqtt", "User sending: "+Data.loggedInUser.getUsername());
                    mqttHandler.korakPoKorakPublish(textView_answer, true, true);
                    //moramo ovde poslati publish
//                    countDownTimer = new CountDownTimer(60000, 1000) {
//                        @Override
//                        public void onTick(long l) {
//                            Log.e("AA","rad3" + l);
//                            Long min = ((l / 1000) % 3600) / 60;
//                            Long sec = (l / 1000) % 60;
//                            String format = String.format(Locale.getDefault(), "%02d:%02d", min, sec);
//                            scoreTimer.setText(format);
//                        }
//
//                        @Override
//                        public void onFinish() {
//                            scoreTimer.setText("00:00");
//                        }
//                    }.start();
                }
            }
//                if(!isFirstRound){//ovaj if ne treba tu da stoji stavio si ga jer imas gore if koji kaze da ako je kraj i odgovor je netacan ond drugi igrac ima pravo da igra pa samo da znas
//                    getParentFragmentManager()
//                            .beginTransaction()
//                            .replace(R.id.fragment_container, new MyNumberFragment())
//                            .setReorderingAllowed(true)
//                            .commit();
//                }
//            }
        }.start();
    }

    @Override
    public void onStop() {
        super.onStop();

        countDownTimer.cancel();
    }
    @Override
    public void onPause() {
        super.onPause();
        SharedPreferences prefs = getActivity().getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("myData", isFirstRound);
        counter1 = 1;
        editor.apply();
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("isFirstRound", isFirstRound);
        super.onSaveInstanceState(outState);
    }
}