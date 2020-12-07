package com.example.sheba_mental_health_project.repository;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.HashMap;
import java.util.Map;

public class AuthRepository {
    private final String TAG ="AuthRepository";
    private static AuthRepository authRepository;
    private AuthRepoLoginPatientInterface mAuthRepoLoginPatientListener;
    private FirebaseAuth mAuth;
    private FirebaseFunctions mFunctions;

    final Context mContext;

    /**<------ Singleton ------>*/
    public static AuthRepository getInstance(final Context context) {
        if (authRepository == null) {
            authRepository = new AuthRepository(context);
        }
        return authRepository;
    }

    private AuthRepository(Context mContext) {
        this.mContext = mContext;
        this.mAuth = FirebaseAuth.getInstance();
        this.mFunctions = FirebaseFunctions.getInstance();
    }

    public Task<HashMap<String, Object>> addMessage(String text) {
        // Create the arguments to the callable function.
        //Map<String, Object> data = new HashMap<>();

        Map<String,Object> data = new HashMap<>();



            data.put("email", "patient@gmail.com");
            data.put("password","matan123");


        return mFunctions
                .getHttpsCallable("createPatient")
                .call(data)
                .continueWith(task -> {
                    // This continuation runs on either success or failure, but if the task
                    // has failed then getResult() will throw an Exception which will be
                    // propagated down.
                    HashMap<String,Object> result = (HashMap<String, Object>) task.getResult().getData();
                  //  Log.d(TAG,result.get("error")+"");
                    return result;
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,e.getMessage());
                    }
                });

    }

    public void loginUser(final String email,final String password,final String role){
        final String THERAPIST = "therapist";
        final String PATIENT = "patient";
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener((Activity) mContext, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkRoleForLogin(email,role);

                        } else {
                            if(role.equals(THERAPIST)){

                            }
                            else if(role.equals(PATIENT)){
                                mAuthRepoLoginPatientListener.onPatientLoginFailed(task.getException().getMessage());
                            }

                        }

                        // ...
                    }
                });
    }

    public void checkRoleForLogin(final String email, final String role) {
        Map<String,Object> data = new HashMap<>();
        data.put("email", email);
        final String THERAPIST = "therapist";
        final String PATIENT = "patient";

         mFunctions
                .getHttpsCallable("checkRole")
                .call(data)
                .continueWith(task -> {
                    HashMap<String,Boolean> rolesMap = (HashMap<String, Boolean>) task.getResult().getData();
                    if(rolesMap.containsKey(role)&&rolesMap.get(role)){
                       if(role.equals(THERAPIST)){

                       }
                       else if(role.equals(PATIENT)){
                            mAuthRepoLoginPatientListener.onPatientLoginSucceed();
                       }
                    }
                    else{
                        if(role.equals(THERAPIST)){

                        }
                        else if(role.equals(PATIENT)){
                            mAuthRepoLoginPatientListener.onPatientLoginFailed("Wrong role");
                        }
                        logOut();

                    }
                    return task.getResult().getData();
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d(TAG,"ERR "+e.getMessage());
                    }
                });

    }

    public void logOut(){
        mAuth.signOut();
        Log.d(TAG, "logOut:"+mAuth.getCurrentUser());
    }

    public interface AuthRepoLoginPatientInterface{
        void onPatientLoginSucceed();
        void onPatientLoginFailed(String message);
    }
    public void setLoginPatientListener(AuthRepoLoginPatientInterface mAuthRepoLoginPatientListener){
        this.mAuthRepoLoginPatientListener = mAuthRepoLoginPatientListener;
    }
}
