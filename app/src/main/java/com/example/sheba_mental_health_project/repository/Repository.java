package com.example.sheba_mental_health_project.repository;

import android.annotation.SuppressLint;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.sheba_mental_health_project.R;
import com.example.sheba_mental_health_project.model.Answer;
import com.example.sheba_mental_health_project.model.AnswerBinary;
import com.example.sheba_mental_health_project.model.AnswerOpen;
import com.example.sheba_mental_health_project.model.Appointment;
import com.example.sheba_mental_health_project.model.ChatMessage;
import com.example.sheba_mental_health_project.model.Feeling;
import com.example.sheba_mental_health_project.model.NotificationsReceiver;
import com.example.sheba_mental_health_project.model.PainPoint;
import com.example.sheba_mental_health_project.model.Patient;
import com.example.sheba_mental_health_project.model.Question;
import com.example.sheba_mental_health_project.model.Therapist;
import com.example.sheba_mental_health_project.model.enums.AppointmentStateEnum;
import com.example.sheba_mental_health_project.model.enums.BodyPartEnum;
import com.example.sheba_mental_health_project.model.enums.QuestionTypeEnum;
import com.example.sheba_mental_health_project.model.enums.ViewModelEnum;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class Repository {

    @SuppressLint("StaticFieldLeak")
    private static Repository repository;

    private final FirebaseFirestore mCloudDB = FirebaseFirestore.getInstance();

    private final Context mContext;

    private final AlarmManager mAlarmManager;

    private Appointment mCurrentAppointment;

    private ListenerRegistration mLiveAppointmentListener;
    private ListenerRegistration mPatientAppointmentsListener;
    private ListenerRegistration mTherapistAppointmentsListener;
    private ListenerRegistration mGetAllPainPointsListener;
    private ListenerRegistration mGetAllPainPointsPhysicalListener;
    private ListenerRegistration mGetLiveAnswersListener;
    private ListenerRegistration mGetFeelingsAnswersListener;

    private final String PATIENTS = "patients";
    private final String THERAPISTS = "therapists";
    private final String APPOINTMENTS = "appointments";
    private final String QUESTIONS = "questions";
    private final String FEELINGS = "feelings";
    private final String FEELINGS_LOCAL = "feelings_local";
    private final String CHAT = "chat";
    private final String QUESTIONS_LOCAL = "questions_local";

    private final String TAG = "Repository";


    /**<------ Interfaces ------>*/
    /*<------ Get All Patients ------>*/
    public interface RepositoryGetAllPatientsInterface {
        void onGetAllPatientsSucceed(List<Patient> patients);

        void onGetAllPatientsFailed(String error);
    }

    private RepositoryGetAllPatientsInterface mRepositoryGetAllPatientsListener;

    public void setGetAllPatientsInterface(RepositoryGetAllPatientsInterface repositoryGetAllPatientsListener){
        this.mRepositoryGetAllPatientsListener = repositoryGetAllPatientsListener;
    }

    /*<------ Get Appointment Of Specific Therapist ------>*/
    public interface RepositoryGetAppointmentOfSpecificTherapistInterface {
        void onGetAppointmentOfSpecificTherapistSucceed(List<Appointment> appointments);

        void onGetAppointmentOfSpecificTherapistFailed(String error);
    }

    private RepositoryGetAppointmentOfSpecificTherapistInterface mRepositoryGetAppointmentOfSpecificTherapistListener;

    public void setGetAppointmentOfSpecificTherapist(RepositoryGetAppointmentOfSpecificTherapistInterface
                                                             repositoryGetAppointmentOfSpecificTherapistListener){
        this.mRepositoryGetAppointmentOfSpecificTherapistListener = repositoryGetAppointmentOfSpecificTherapistListener;
    }

    /*<------ Get Appointment Of Specific Patient ------>*/
    public interface RepositoryGetAppointmentOfSpecificPatientInterface {
        void onGetAppointmentOfSpecificPatientSucceed(List<Appointment> appointments);

        void onGetAppointmentOfSpecificPatientFailed(String error);
    }

    private RepositoryGetAppointmentOfSpecificPatientInterface mRepositoryGetAppointmentOfSpecificPatientListener;

    public void setGetAppointmentOfSpecificPatient(RepositoryGetAppointmentOfSpecificPatientInterface
                                                           repositoryGetAppointmentOfSpecificPatientListener){
        this.mRepositoryGetAppointmentOfSpecificPatientListener = repositoryGetAppointmentOfSpecificPatientListener;
    }

    /*<------ Get All Pain Points ------>*/
    public interface RepositoryGetAllPainPointsInterface {
        void onGetAllPainPointsSucceed(Map<String, List<PainPoint>> painPointsMap);

        void onGetAllPainPointsFailed(String error);
    }

    private RepositoryGetAllPainPointsInterface mRepositoryGetAllPainPointsListener;

    public void setGetAllPainPointsInterface(RepositoryGetAllPainPointsInterface repositoryGetAllPainPointsListener){
        this.mRepositoryGetAllPainPointsListener = repositoryGetAllPainPointsListener;
    }

    /*<------ Get All Pain Points for Physical State ------>*/
    public interface RepositoryGetAllPainPointsPhysicalInterface {
        void onGetAllPainPointsPhysicalSucceed(Map<String, List<PainPoint>> painPointsMap);

        void onGetAllPainPointsPhysicalFailed(String error);
    }

    private RepositoryGetAllPainPointsPhysicalInterface mRepositoryGetAllPainPointsPhysicalListener;

    public void setGetAllPainPointsPhysicalInterface(RepositoryGetAllPainPointsPhysicalInterface
                                                             repositoryGetAllPainPointsPhysicalInterface){
        this.mRepositoryGetAllPainPointsPhysicalListener = repositoryGetAllPainPointsPhysicalInterface;
    }

    /*<------ Add Appointment ------>*/
    public interface RepositoryAddAppointmentInterface {
        void onAddAppointmentSucceed(String appointmentId);

        void onAddAppointmentFailed(String error);
    }

    private RepositoryAddAppointmentInterface mRepositoryAddAppointmentListener;

    public void setAddAppointmentInterface(RepositoryAddAppointmentInterface repositoryAddAppointmentListener) {
        this.mRepositoryAddAppointmentListener = repositoryAddAppointmentListener;
    }

    /*<------ Update Appointment ------>*/
    public interface RepositoryUpdateAppointmentInterface {
        void onUpdateAppointmentSucceed(String appointmentId);

        void onUpdateAppointmentFailed(String error);
    }

    private RepositoryUpdateAppointmentInterface mRepositoryUpdateAppointmentListener;

    public void setUpdateAppointmentInterface(RepositoryUpdateAppointmentInterface repositoryUpdateAppointmentListener) {
        this.mRepositoryUpdateAppointmentListener = repositoryUpdateAppointmentListener;
    }

    /*<------ Delete Appointment ------>*/
    public interface RepositoryDeleteAppointmentInterface {
        void onDeleteAppointmentSucceed(Appointment appointment);

        void onDeleteAppointmentFailed(String error);
    }

    private RepositoryDeleteAppointmentInterface mRepositoryDeleteAppointmentListener;

    public void setDeleteAppointmentInterface(RepositoryDeleteAppointmentInterface repositoryDeleteAppointmentListener) {
        this.mRepositoryDeleteAppointmentListener = repositoryDeleteAppointmentListener;
    }

    /*<------ Get Last Appointment ------>*/
    public interface RepositoryGetLastAppointmentInterface {
        void onGetLastAppointmentSucceed(Appointment lastAppointment);
        void onGetLastAppointmentFailed(String error);
    }

    private RepositoryGetLastAppointmentInterface mRepositoryGetLastAppointmentInterface;

    public void setGetLastAppointmentInterface(RepositoryGetLastAppointmentInterface lastAppointmentInterface) {
        this.mRepositoryGetLastAppointmentInterface = lastAppointmentInterface;
    }

    /*<------ Get Live Appointment ------>*/
    public interface RepositoryGetLiveAppointmentInterface {
        void onGetLiveAppointmentSucceed(Appointment appointment);
        void onGetLiveAppointmentFailed(String error);
    }

    private RepositoryGetLiveAppointmentInterface mRepositoryGetLiveAppointmentInterface;

    public void setGetLiveAppointmentInterface(RepositoryGetLiveAppointmentInterface repositoryGetLiveAppointmentInterface) {
        this.mRepositoryGetLiveAppointmentInterface = repositoryGetLiveAppointmentInterface;
    }

    /*<------ Set Pain Points ------>*/
    public interface RepositorySetPainPointsInterface {
        void onSetPainPointsSucceed(PainPoint painPoint);

        void onSetPainPointsFailed(String error);
    }

    private RepositorySetPainPointsInterface mRepositorySetPainPointsListener;

    public void setSetPainPointsInterface(RepositorySetPainPointsInterface repositorySetPainPointsInterface) {
        this.mRepositorySetPainPointsListener = repositorySetPainPointsInterface;
    }

    /*<------ Delete Pain Point ------>*/
    public interface RepositoryDeletePainPointInterface {
        void onDeletePainPointSucceed(PainPoint painPoint);

        void onDeletePainPointFailed(String error);
    }

    private RepositoryDeletePainPointInterface mRepositoryDeletePainPointListener;

    public void setDeletePainPointInterface(RepositoryDeletePainPointInterface repositoryDeletePainPointInterface) {
        this.mRepositoryDeletePainPointListener = repositoryDeletePainPointInterface;
    }

    /*<------ Get Questions of Page ------>*/
    public interface RepositoryGetQuestionsOfPageInterface {
        void onGetQuestionsOfPageSucceed(List<Question> questions);

        void onGetQuestionsOfPageFailed(String error);
    }

    private RepositoryGetQuestionsOfPageInterface mRepositoryGetQuestionsOfPageListener;

    public void setGetQuestionsOfPageInterface(RepositoryGetQuestionsOfPageInterface
                                                       repositoryGetQuestionsOfPageInterface) {
        this.mRepositoryGetQuestionsOfPageListener = repositoryGetQuestionsOfPageInterface;
    }

    /*<------ Get Live Answers ------>*/
    public interface RepositoryGetLiveAnswersInterface {
        void onGetLiveAnswersSucceed(List<Answer> answers);

        void onGetLiveAnswersFailed(String error);
    }

    private RepositoryGetLiveAnswersInterface mRepositoryGetLiveAnswersListener;

    public void setGetLiveAnswersInterface(RepositoryGetLiveAnswersInterface repositoryGetLiveAnswersInterface) {
        this.mRepositoryGetLiveAnswersListener = repositoryGetLiveAnswersInterface;
    }

    /*<------ Get Feelings Answers ------>*/
    public interface RepositoryGetFeelingsAnswersInterface {
        void onGetFeelingsAnswersSucceed(Map<String, Integer> feelingsAnswers);

        void onGetFeelingsAnswersFailed(String error);
    }

    private RepositoryGetFeelingsAnswersInterface mRepositoryGetFeelingsAnswersListener;

    public void setGetFeelingsAnswersInterface(RepositoryGetFeelingsAnswersInterface
                                                       repositoryGetFeelingsAnswersInterface) {
        this.mRepositoryGetFeelingsAnswersListener = repositoryGetFeelingsAnswersInterface;
    }

    /*<------ Get Patient Feelings ------>*/
    public interface RepositoryGetPatientFeelingsInterface {
        void onGetPatientFeelingsSucceed(List<Feeling> feelings);

        void onGetPatientFeelingsFailed(String error);
    }

    private RepositoryGetPatientFeelingsInterface mRepositoryGetPatientFeelingsListener;

    public void setGetPatientFeelingsInterface(RepositoryGetPatientFeelingsInterface
                                                       repositoryGetPatientFeelingsInterface) {
        this.mRepositoryGetPatientFeelingsListener = repositoryGetPatientFeelingsInterface;
    }

    /*<------ Update Answers of Appointment ------>*/
    public interface RepositoryUpdateAnswersOfAppointmentInterface {
        void onUpdateAnswersOfAppointmentSucceed(Appointment appointment);

        void onUpdateAnswersOfAppointmentFailed(String error);
    }

    private RepositoryUpdateAnswersOfAppointmentInterface mRepositoryUpdateAnswersOfAppointmentListener;

    public void setUpdateAnswersOfAppointmentInterface(RepositoryUpdateAnswersOfAppointmentInterface
                                                               repositoryUpdateAnswersOfAppointmentInterface){
        this.mRepositoryUpdateAnswersOfAppointmentListener = repositoryUpdateAnswersOfAppointmentInterface;
    }

    /*<------ Update Answers of Feelings ------>*/
    public interface RepositoryUpdateAnswersOfFeelingsInterface {
        void onUpdateAnswersOfFeelingsSucceed(Appointment appointment);

        void onUpdateAnswersOfFeelingsFailed(String error);
    }

    private RepositoryUpdateAnswersOfFeelingsInterface mRepositoryUpdateAnswersOfFeelingsListener;

    public void setUpdateAnswersOfFeelingsInterface(RepositoryUpdateAnswersOfFeelingsInterface
                                                            repositoryUpdateAnswersOfFeelingsInterface){
        this.mRepositoryUpdateAnswersOfFeelingsListener = repositoryUpdateAnswersOfFeelingsInterface;
    }

    /*<------ Update State of Appointment ------>*/
    public interface RepositoryUpdateStateOfAppointmentInterface {
        void onUpdateStateOfAppointmentSucceed(AppointmentStateEnum appointmentState);

        void onUpdateAnswersOfAppointmentFailed(String error);
    }

    private RepositoryUpdateStateOfAppointmentInterface mRepositoryUpdateStateOfAppointmentListener;

    public void setUpdateStateOfAppointmentInterface(RepositoryUpdateStateOfAppointmentInterface
                                                             repositoryUpdateStateOfAppointmentInterface){
        this.mRepositoryUpdateStateOfAppointmentListener = repositoryUpdateStateOfAppointmentInterface;
    }

    /*<------ Update Finished Pre Questions ------>*/
    public interface RepositoryUpdateFinishedPreQuestionsInterface {
        void onUpdateFinishedPreQuestionsSucceed(boolean isFinishedPreQuestions);

        void onUpdateFinishedPreQuestionsFailed(String error);
    }

    private RepositoryUpdateFinishedPreQuestionsInterface mRepositoryUpdateFinishedPreQuestionsListener;

    public void setUpdateFinishedPreQuestionsInterface(RepositoryUpdateFinishedPreQuestionsInterface
                                                               repositoryUpdateFinishedPreQuestionsInterface){
        this.mRepositoryUpdateFinishedPreQuestionsListener = repositoryUpdateFinishedPreQuestionsInterface;
    }

    /*<------ Upload Chat Message ------>*/
    public interface RepositoryUploadChatMessageInterface {
        void onUploadChatMessageFailed(String error);
    }

    private RepositoryUploadChatMessageInterface mRepositoryUploadChatMessageListener;

    public void setUploadChatMessageInterface(RepositoryUploadChatMessageInterface
                                                      repositoryUploadChatMessageInterface){
        this.mRepositoryUploadChatMessageListener = repositoryUploadChatMessageInterface;
    }

    /**<------ Singleton ------>*/
    public static Repository getInstance(final Context context) {
        if (repository == null) {
            repository = new Repository(context);
        }
        return repository;
    }

    private Repository(final Context context) {
        this.mContext = context;
        this.mAlarmManager = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
//        addQuestionsEnglish();
//        addQuestionsHebrew();
    }

    public void getAllPatients() {
        final List<Patient> patients = new ArrayList<>();

        mCloudDB.collection(PATIENTS).get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                patients.add(document.toObject(Patient.class));
                            }
                            if (mRepositoryGetAllPatientsListener != null) {
                                mRepositoryGetAllPatientsListener.onGetAllPatientsSucceed(patients);
                            }
                        } else {
                            final String error = task.getException().getMessage();
                            Log.wtf(TAG, "onComplete: ", task.getException());
                            if (mRepositoryGetAllPatientsListener != null) {
                                mRepositoryGetAllPatientsListener.onGetAllPatientsFailed(error);
                            }
                        }
                    }
                });
    }

    public void getPatientsOfSpecificTherapist() {
        final Set<Patient> patients = new HashSet<>();

        final String therapistId = AuthRepository.getInstance(mContext).getUser().getId();

        mCloudDB.collection(APPOINTMENTS)
                .whereEqualTo(FieldPath.of("therapist", "id"), therapistId)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task
                                    .getResult())) {
                                final Appointment appointment = document.toObject(Appointment.class);
                                final Patient patient = appointment.getPatient();
                                patients.add(patient);
                            }

                            if (mRepositoryGetAllPatientsListener != null) {
                                mRepositoryGetAllPatientsListener
                                        .onGetAllPatientsSucceed(new ArrayList<>(patients));
                            }
                        } else {
                            final String error = Objects.requireNonNull(task.getException())
                                    .getMessage();
                            Log.wtf(TAG, "onComplete: ", task.getException());

                            if (mRepositoryGetAllPatientsListener != null) {
                                mRepositoryGetAllPatientsListener.onGetAllPatientsFailed(error);
                            }
                        }
                    }
                });
    }

    public void addAppointment(final Appointment appointment) {
        final String id = mCloudDB.collection(APPOINTMENTS).document().getId();
        appointment.setId(id);
        appointment.setTherapist((Therapist) AuthRepository.getInstance(mContext).getUser());

        mCloudDB.collection(APPOINTMENTS)
                .document(id)
                .set(appointment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "onComplete: " + id);
                            if (mRepositoryAddAppointmentListener != null) {
                                mRepositoryAddAppointmentListener.onAddAppointmentSucceed(id);
                            }
                        } else {
                            final String error = Objects
                                    .requireNonNull(task.getException()).getMessage();
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryAddAppointmentListener != null) {
                                mRepositoryAddAppointmentListener.onAddAppointmentFailed(error);
                            }
                        }
                    }
                });
    }

    public void updateAppointment(final Appointment appointment) {
        mCloudDB.collection(APPOINTMENTS)
                .document(appointment.getId())
                .set(appointment)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            setCurrentAppointment(appointment);
                            cancelAppointmentNotificationByDate(appointment);
                            if (mRepositoryUpdateAppointmentListener != null) {
                                mRepositoryUpdateAppointmentListener
                                        .onUpdateAppointmentSucceed(appointment.getId());
                            }
                        } else {
                            final String error = Objects
                                    .requireNonNull(task.getException()).getMessage();
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryUpdateAppointmentListener != null) {
                                mRepositoryUpdateAppointmentListener.onUpdateAppointmentFailed(error);
                            }
                        }
                    }
                });
    }

    public void deleteAppointment() {
        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .delete()
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            cancelAppointmentNotificationByDate(mCurrentAppointment);
                            if (mRepositoryDeleteAppointmentListener != null) {
                                mRepositoryDeleteAppointmentListener
                                        .onDeleteAppointmentSucceed(mCurrentAppointment);
                            }
                        } else {
                            final String error = Objects
                                    .requireNonNull(task.getException()).getMessage();
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryDeleteAppointmentListener != null) {
                                mRepositoryDeleteAppointmentListener.onDeleteAppointmentFailed(error);
                            }
                        }
                    }
                });
    }

    public void getAppointmentsOfSpecificTherapist() {
        final String id = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        final List<AppointmentStateEnum> stateQuery = new ArrayList<>();
        stateQuery.add(AppointmentStateEnum.PreMeeting);
        stateQuery.add(AppointmentStateEnum.OnGoing);

        mTherapistAppointmentsListener = mCloudDB.collection(APPOINTMENTS)
                .whereEqualTo(FieldPath.of("therapist", "id"), id)
                .whereIn("state", stateQuery)
                .orderBy("appointmentDate")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null && value != null) {
                            final List<Appointment> appointments = new ArrayList<>();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                List<Map<String, Object>> answersMapList =
                                        (List<Map<String, Object>>) document.get("answers");
                                final Appointment appointment = document.toObject(Appointment.class);
                                mappingAnswerObject(answersMapList, appointment);
                                appointments.add(appointment);
                                setAppointmentNotificationByDate(appointment, false);
                            }
                            Log.d(TAG, "onEvent: " + appointments.size());
                            if (mRepositoryGetAppointmentOfSpecificTherapistListener != null) {
                                mRepositoryGetAppointmentOfSpecificTherapistListener
                                        .onGetAppointmentOfSpecificTherapistSucceed(appointments);
                            }
                        } else {
                            String errorMessage;
                            if (error != null) {
                                errorMessage = error.getMessage();
                            } else {
                                errorMessage = "FIREBASE ERROR IS NULL";
                            }
                            Log.w(TAG, "onEvent: ", error);
                            if (mRepositoryGetAppointmentOfSpecificTherapistListener != null) {
                                mRepositoryGetAppointmentOfSpecificTherapistListener
                                        .onGetAppointmentOfSpecificTherapistFailed(errorMessage);
                            }
                        }
                    }
                });
    }

    public void getAppointmentsOfSpecificPatient(final String id, final List<AppointmentStateEnum> stateQuery) {
        mPatientAppointmentsListener = mCloudDB.collection(APPOINTMENTS)
                .whereEqualTo(FieldPath.of("patient", "id"), id)
                .whereIn("state", stateQuery)
                .orderBy("appointmentDate")
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (error == null && value != null) {
                            final List<Appointment> appointments = new ArrayList<>();
                            for (DocumentSnapshot document : value.getDocuments()) {
                                List<Map<String, Object>> answersMapList =
                                        (List<Map<String, Object>>) document.get("answers");
                                final Appointment appointment = document.toObject(Appointment.class);
                                mappingAnswerObject(answersMapList, appointment);
                                appointments.add(appointment);
                                setAppointmentNotificationByDate(appointment, true);
                            }
                            Log.d(TAG, "onEvent: appointments size: " + appointments.size());
                            if (mRepositoryGetAppointmentOfSpecificPatientListener != null) {
                                mRepositoryGetAppointmentOfSpecificPatientListener
                                        .onGetAppointmentOfSpecificPatientSucceed(appointments);
                            }
                        } else {
                            String errorMessage;
                            if (error != null) {
                                errorMessage = error.getMessage();
                            } else {
                                errorMessage = "FIREBASE ERROR IS NULL";
                            }
                            Log.w(TAG, "onEvent: ", error);
                            if (mRepositoryGetAppointmentOfSpecificPatientListener != null) {
                                mRepositoryGetAppointmentOfSpecificPatientListener
                                        .onGetAppointmentOfSpecificPatientFailed(errorMessage);
                            }
                        }
                    }
                });
    }

    public void getLiveAppointmentState() {
        mLiveAppointmentListener = mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (error == null && value != null) {
                            final Appointment appointment = value.toObject(Appointment.class);

                            if (mRepositoryGetLiveAppointmentInterface != null) {
                                mRepositoryGetLiveAppointmentInterface
                                        .onGetLiveAppointmentSucceed(appointment);
                            }
                        } else if (error != null) {
                            Log.e(TAG, "onEvent: ", error);

                            if (mRepositoryGetLiveAppointmentInterface != null) {
                                mRepositoryGetLiveAppointmentInterface
                                        .onGetLiveAppointmentFailed(error.getMessage());
                            }
                        } else {
                            Log.e(TAG, "onEvent: " +
                                    "SOMETHING WENT WRONG: null firebase error");
                        }
                    }
                });
    }

    public void removeLiveAppointmentListener() {
        if (mLiveAppointmentListener != null) {
            mLiveAppointmentListener.remove();
        }
    }

    private void mappingAnswerObject(final List<Map<String, Object>> answersMapList,
                                     final Appointment appointment) {
        if (answersMapList != null && !answersMapList.isEmpty()) {
            appointment.getAnswers().clear();
            for(Map<String, Object> map : answersMapList){
                final Object answer = map.get("answer");
                final String id = (String) map.get("id");
                if (answer instanceof Boolean) {
                    final String answerDetails = (String) map.get("answerDetails");
                    appointment.getAnswers().add(new AnswerBinary(id, (Boolean) answer,
                            answerDetails));
                } else {
                    appointment.getAnswers().add(new AnswerOpen(id, (String) answer));
                }
            }
        }
    }

    public Appointment getCurrentAppointment() {
        return mCurrentAppointment;
    }

    public void setCurrentAppointment(Appointment currentAppointment) {
        this.mCurrentAppointment = currentAppointment;
    }

    public void setPainPoints(final BodyPartEnum bodyPart, final PainPoint painPoint) {
        final Map<String, List<PainPoint>> map =  mCurrentAppointment.getPainPointsOfBodyPartMap();
        List<PainPoint> painPoints = map.get(bodyPart.name());
        if (painPoints == null) {
            painPoints = new ArrayList<>();
            painPoints.add(painPoint);
            map.put(bodyPart.name(), painPoints);
        } else {
            final int index = painPoints.indexOf(painPoint);
            if (index != -1) {
                painPoints.set(index, painPoint);
            } else {
                painPoints.add(painPoint);
            }
        }

        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .update("painPointsOfBodyPartMap", map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (mRepositorySetPainPointsListener != null) {
                                mRepositorySetPainPointsListener.onSetPainPointsSucceed(painPoint);
                            }
                        } else {
                            final String error = Objects.requireNonNull(task.getException())
                                    .getMessage();
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositorySetPainPointsListener != null) {
                                mRepositorySetPainPointsListener.onSetPainPointsFailed(error);
                            }
                        }
                    }
                });
    }

    public void deletePainPoint(final BodyPartEnum bodyPart, final PainPoint painPoint) {
        final Map<String, List<PainPoint>> map =  mCurrentAppointment.getPainPointsOfBodyPartMap();
        List<PainPoint> painPoints = map.get(bodyPart.name());

        if (painPoints != null) {
            painPoints.remove(painPoint);
        }

        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .update("painPointsOfBodyPartMap", map)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (mRepositoryDeletePainPointListener != null) {
                                mRepositoryDeletePainPointListener.onDeletePainPointSucceed(painPoint);
                            }
                        } else {
                            final String error = Objects.requireNonNull(task.getException())
                                    .getMessage();
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryDeletePainPointListener != null) {
                                mRepositoryDeletePainPointListener.onDeletePainPointFailed(error);
                            }
                        }
                    }
                });
    }

    public List<PainPoint> getSpecificPainPointsList(final BodyPartEnum bodyPartEnum) {
        List<PainPoint> painPoints = mCurrentAppointment.getPainPointsOfBodyPartMap().get(bodyPartEnum.name());
        if (painPoints == null) {
            painPoints = new ArrayList<>();
        }
        return painPoints;
    }

    public void getAllPainPoints(final Appointment appointment) {
        mGetAllPainPointsListener = mCloudDB.collection(APPOINTMENTS)
                .document(appointment.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            final Appointment dbAppointment = value.toObject(Appointment.class);
                            if (mRepositoryGetAllPainPointsListener != null) {
                                mRepositoryGetAllPainPointsListener
                                        .onGetAllPainPointsSucceed(dbAppointment.getPainPointsOfBodyPartMap());
                                appointment.setPainPointsOfBodyPartMap(dbAppointment.getPainPointsOfBodyPartMap());
                            }
                        } else {
                            Log.w(TAG, "onEvent: ", error);
                            if (mRepositoryGetAllPainPointsListener != null) {
                                mRepositoryGetAllPainPointsListener.onGetAllPainPointsFailed(error.getMessage());
                            }
                        }
                    }
                });
    }

    public void getAllPainPointsPhysical(final Appointment appointment) {
        mGetAllPainPointsPhysicalListener = mCloudDB.collection(APPOINTMENTS)
                .document(appointment.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            final Appointment dbAppointment = value.toObject(Appointment.class);
                            if (mRepositoryGetAllPainPointsPhysicalListener != null) {
                                mRepositoryGetAllPainPointsPhysicalListener
                                        .onGetAllPainPointsPhysicalSucceed(dbAppointment.getPainPointsOfBodyPartMap());
                                appointment.setPainPointsOfBodyPartMap(dbAppointment.getPainPointsOfBodyPartMap());
                            }
                        } else {
                            Log.w(TAG, "onEvent: ", error);
                            if (mRepositoryGetAllPainPointsPhysicalListener != null) {
                                mRepositoryGetAllPainPointsPhysicalListener
                                        .onGetAllPainPointsPhysicalFailed(error.getMessage());
                            }
                        }
                    }
                });
    }

    public void removeTherapistAppointmentsListener() {
        mTherapistAppointmentsListener.remove();
    }

    public void removePatientAppointmentsListener() {
        mPatientAppointmentsListener.remove();
    }

    public void removeGetAllPainPointsListener() {
        mGetAllPainPointsListener.remove();
    }

    public void removeGetAllPainPointsPhysicalListener() {
        mGetAllPainPointsPhysicalListener.remove();
    }

    public void getFeelingsAnswers(final Appointment appointment) {
        mGetFeelingsAnswersListener = mCloudDB.collection(APPOINTMENTS)
                .document(appointment.getId())
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            final Appointment dbAppointment = value.toObject(Appointment.class);
                            if (mRepositoryGetFeelingsAnswersListener != null) {
                                mRepositoryGetFeelingsAnswersListener
                                        .onGetFeelingsAnswersSucceed(dbAppointment.getFeelingsAnswersMap());

                                appointment.setFeelingsAnswersMap(dbAppointment.getFeelingsAnswersMap());
                            }
                        } else {
                            Log.w(TAG, "onEvent: ", error);
                            if (mRepositoryGetFeelingsAnswersListener != null) {
                                mRepositoryGetFeelingsAnswersListener
                                        .onGetFeelingsAnswersFailed(error.getMessage());
                            }
                        }
                    }
                });
    }

    public void removeGetFeelingsAnswersListener() {
        mGetFeelingsAnswersListener.remove();
    }

    public void getQuestionsByPage(final ViewModelEnum page) {
        String language = Locale.getDefault().getLanguage();
        if (language.equals(new Locale("he").getLanguage())) {
            language = "he";
        }
        if (language.equals(new Locale("en").getLanguage())) {
            language = "en";
        }

        final List<Question> questions = new ArrayList<>();

        mCloudDB.collection(QUESTIONS)
                .document(language)
                .collection(QUESTIONS_LOCAL)
                .whereEqualTo("page", page.name())
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                questions.add(document.toObject(Question.class));
                            }
                            if (mRepositoryGetQuestionsOfPageListener != null) {
                                mRepositoryGetQuestionsOfPageListener.onGetQuestionsOfPageSucceed(questions);
                            }
                        } else {
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryGetQuestionsOfPageListener != null) {
                                mRepositoryGetQuestionsOfPageListener.onGetQuestionsOfPageFailed(Objects.
                                        requireNonNull(task.getException()).getMessage());
                            }
                        }
                    }
                });
    }

    public void getAllQuestions() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals(new Locale("he").getLanguage())) {
            language = "he";
        }
        if (language.equals(new Locale("en").getLanguage())) {
            language = "en";
        }

        final List<Question> questions = new ArrayList<>();

        mCloudDB.collection(QUESTIONS)
                .document(language)
                .collection(QUESTIONS_LOCAL)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                questions.add(document.toObject(Question.class));
                            }
                            if (mRepositoryGetQuestionsOfPageListener != null) {
                                mRepositoryGetQuestionsOfPageListener.onGetQuestionsOfPageSucceed(questions);
                            }
                        } else {
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryGetQuestionsOfPageListener != null) {
                                mRepositoryGetQuestionsOfPageListener.onGetQuestionsOfPageFailed(Objects.
                                        requireNonNull(task.getException()).getMessage());
                            }
                        }
                    }
                });
    }

    public void getLiveAnswersOfAppointment(final String appointmentId) {
        mGetLiveAnswersListener = mCloudDB.collection(APPOINTMENTS)
                .document(appointmentId)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value,
                                        @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists()) {
                            final Appointment dbAppointment = value.toObject(Appointment.class);
                            List<Map<String, Object>> answersMapList =
                                    (List<Map<String, Object>>) value.get("answers");
                            mappingAnswerObject(answersMapList, dbAppointment);

                            if (dbAppointment != null) {
                                if (mRepositoryGetLiveAnswersListener != null) {
                                    mRepositoryGetLiveAnswersListener
                                            .onGetLiveAnswersSucceed(dbAppointment.getAnswers());
                                }
                            }
                        } else if (error != null) {
                            Log.w(TAG, "onEvent: ", error);

                            if (mRepositoryGetLiveAnswersListener != null) {
                                mRepositoryGetLiveAnswersListener
                                        .onGetLiveAnswersFailed(error.getMessage());
                            }
                        } else {
                            Log.w(TAG, "onEvent: NULL FIREBASE ERROR");
                        }
                    }
                });
    }

    public void removeLiveAnswersListener() {
        mGetLiveAnswersListener.remove();
    }

    public void getFeelings() {
        String language = Locale.getDefault().getLanguage();
        if (language.equals(new Locale("he").getLanguage())) {
            language = "he";
        }
        if (language.equals(new Locale("en").getLanguage())) {
            language = "en";
        }

        final List<Feeling> feelings = new ArrayList<>();

        mCloudDB.collection(FEELINGS)
                .document(language)
                .collection(FEELINGS_LOCAL)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : Objects.requireNonNull(task.getResult())) {
                                feelings.add(document.toObject(Feeling.class));
                            }
                            if (mRepositoryGetPatientFeelingsListener != null) {
                                mRepositoryGetPatientFeelingsListener.onGetPatientFeelingsSucceed(feelings);
                            }
                        } else {
                            Log.w(TAG, "onComplete: ", task.getException());
                            if (mRepositoryGetPatientFeelingsListener != null) {
                                mRepositoryGetPatientFeelingsListener.onGetPatientFeelingsFailed(Objects.
                                        requireNonNull(task.getException()).getMessage());
                            }
                        }
                    }
                });
    }

    public void updateAnswersOfAppointment() {
        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .update("answers", mCurrentAppointment.getAnswers())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (mRepositoryUpdateAnswersOfAppointmentListener != null) {
                                mRepositoryUpdateAnswersOfAppointmentListener
                                        .onUpdateAnswersOfAppointmentSucceed(mCurrentAppointment);
                            }
                        } else {
                            Log.e(TAG, "onComplete: ", task.getException());
                            if (mRepositoryUpdateAnswersOfAppointmentListener != null) {
                                mRepositoryUpdateAnswersOfAppointmentListener.onUpdateAnswersOfAppointmentFailed(Objects.
                                        requireNonNull(task.getException()).getMessage());
                            }
                        }
                    }
                });
    }

    public void updateAnswersOfFeelings() {
        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .update("feelingsAnswersMap", mCurrentAppointment.getFeelingsAnswersMap())
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            if (mRepositoryUpdateAnswersOfFeelingsListener != null) {
                                mRepositoryUpdateAnswersOfFeelingsListener
                                        .onUpdateAnswersOfFeelingsSucceed(mCurrentAppointment);
                            }
                        } else {
                            Log.e(TAG, "onComplete: ", task.getException());
                            if (mRepositoryUpdateAnswersOfFeelingsListener != null) {
                                mRepositoryUpdateAnswersOfFeelingsListener
                                        .onUpdateAnswersOfFeelingsFailed(Objects.
                                        requireNonNull(task.getException()).getMessage());
                            }
                        }
                    }
                });
    }

    public void updateFinishedPreQuestions() {
        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .update("isFinishedPreQuestions", true)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            mCurrentAppointment.setIsFinishedPreQuestions(true);
                            if (mRepositoryUpdateFinishedPreQuestionsListener != null) {
                                mRepositoryUpdateFinishedPreQuestionsListener
                                        .onUpdateFinishedPreQuestionsSucceed(true);
                            }
                        } else {
                            Log.e(TAG, "onComplete: ", task.getException());
                            if (mRepositoryUpdateFinishedPreQuestionsListener != null) {
                                mRepositoryUpdateFinishedPreQuestionsListener
                                        .onUpdateFinishedPreQuestionsFailed(task
                                                .getException().getMessage());
                            }
                        }
                    }
                });
    }

    public Query getChatQuery() {
        return mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .collection(CHAT)
                .orderBy("time");
    }

    public void uploadChatMessageToCloud(final ChatMessage chatMessage) {
        mCloudDB.collection(APPOINTMENTS)
                .document(mCurrentAppointment.getId())
                .collection(CHAT)
                .document(chatMessage.getTime().toString())
                .set(chatMessage)
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.e(TAG, "onFailure: ", e);
                        if (mRepositoryUploadChatMessageListener != null) {
                            mRepositoryUploadChatMessageListener.onUploadChatMessageFailed(e.getMessage());
                        }
                    }
                });
    }

    public void getLastAppointment() {
        final String patientId = mCurrentAppointment.getPatient().getId();
        final List<AppointmentStateEnum> stateQuery = new ArrayList<>();
        stateQuery.add(AppointmentStateEnum.Ended);

        mCloudDB.collection(APPOINTMENTS)
                .whereIn("state", stateQuery)
                .whereEqualTo(FieldPath.of("patient", "id"), patientId)
                .orderBy("appointmentDate", Query.Direction.ASCENDING).limit(1)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {

                if(value != null && !value.isEmpty()){
                    Appointment appointment = value.getDocuments().get(0).toObject(Appointment.class);
                    if (mRepositoryGetLastAppointmentInterface != null) {
                        mRepositoryGetLastAppointmentInterface.onGetLastAppointmentSucceed(appointment);
                    }
                }
                else {
                    if (mRepositoryGetLastAppointmentInterface != null) {
                        if(error!=null)
                            mRepositoryGetLastAppointmentInterface.onGetLastAppointmentFailed(error.getMessage());
                        else
                            mRepositoryGetLastAppointmentInterface.onGetLastAppointmentFailed("No Appointment found");
                    }
                }
            }
        });
    }

    public void updateAppointmentState(AppointmentStateEnum appointmentStateEnum) {
        mCloudDB.collection(APPOINTMENTS).document(mCurrentAppointment.getId())
                .update("state", appointmentStateEnum)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    mCurrentAppointment.setState(appointmentStateEnum);
                    if (mRepositoryUpdateStateOfAppointmentListener != null) {
                        mRepositoryUpdateStateOfAppointmentListener
                                .onUpdateStateOfAppointmentSucceed(appointmentStateEnum);
                    }
                } else {
                    if (mRepositoryUpdateStateOfAppointmentListener != null) {
                        mRepositoryUpdateStateOfAppointmentListener
                                .onUpdateAnswersOfAppointmentFailed(task.getException().getMessage());
                    }
                }
            }
        });
    }

    private void setAppointmentNotificationByDate(final Appointment appointment,
                                                  final boolean isPatient) {
        final long minuteMs = 60_000L;
        final Date now = new Date(System.currentTimeMillis() + (10 * minuteMs));
        if (appointment.getAppointmentDate() != null &&
                appointment.getAppointmentDate().after(now)) {
            final long alarmTimeMs = appointment.getAppointmentDate().getTime() - (10 * minuteMs);
            final int requestCode = (int) appointment.getAppointmentDate().getTime();

            final Bundle bundle = new Bundle();
            final Intent intent = new Intent(mContext, NotificationsReceiver.class);
            bundle.putSerializable("appointment", appointment);
            bundle.putBoolean("is_patient", isPatient);
            intent.putExtra("bundle", bundle);

            final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode,
                    intent, PendingIntent.FLAG_CANCEL_CURRENT);

            mAlarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTimeMs, pendingIntent);
        }
    }

    private void cancelAppointmentNotificationByDate(final Appointment appointment) {
        final int requestCode = (int) appointment.getAppointmentDate().getTime();

        final Intent intent = new Intent(mContext, NotificationsReceiver.class);

        final PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, requestCode,
                intent, PendingIntent.FLAG_CANCEL_CURRENT);

        mAlarmManager.cancel(pendingIntent);
    }

    public void addQuestionsEnglish() {
        final List<Question> questions = new ArrayList<>();

        questions.add(new Question("1", "I Want a Long Meeting",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));
        questions.add(new Question("10", "I Want a Short Meeting",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));
        questions.add(new Question("11", "I Want Another Person to Be Present During the Meeting",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));

        questions.add(new Question("12", "Document 17",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("13", "Document 17 Extension",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("14", "Appointment Summery",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("15", "Prescriptions",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("16", "Document for Social Security",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("17", "Document for Family Doctor",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("18", "Document for Rehabilitation Committee",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("19", "Document for Occupational Physician",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("2", "Request for Psychological Treatment",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("20", "Document for a Social Worker",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("21", "Other",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));

        questions.add(new Question("22", "I Have One of These Symptoms: Fever, Sore Throat, Loss of Taste or Smell, Dry Cough, Muscle pain.",
                QuestionTypeEnum.Binary, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("23", "I Am Diagnosed with COVID-19",
                QuestionTypeEnum.Binary, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("24", "One or More of My Household Members Have Been Diagnosed with COVID-19",
                QuestionTypeEnum.Binary, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("25", "Does Any of Your Relatives Has Been Diagnosed with COVID-19?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("26", "Were You Diagnosed with COVID-19 in the Past? If Yes, When?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("27", "Have You Experienced Any Emotional Suffering Due to COVID-19?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("28", "Have You Experienced Any Physical Suffering Due to COVID-19?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("29", "Have You Experienced Any Emotional Suffering Due to COVID-19? (While Not being Diagnosed Yourself)",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("3", "Have You Experienced Any Physical Suffering Due to COVID-19? (While Not being Diagnosed Yourself)",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));

        questions.add(new Question("30", "I Feel Suicidal",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));
        questions.add(new Question("31", "I Feel Aggressive",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));
        questions.add(new Question("32", "I Hear Voices",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));
        questions.add(new Question("33", "I Feel Strong Physical Pain",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));

        questions.add(new Question("34", "I Had an Accident Recently",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("35", "I Have been Hospitalized Recently",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("350", "There Has Been a Change In My Medicine Dosage",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("351", "I Have Stopped Taking One of My Medicines",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("352", "I Have a New Medical Diagnose",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("353", "I was Injured Recently",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("354", "I Am Currently During a Legal Proceeding",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("355", "I Went to ER Recently",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));

        questions.add(new Question("36", "I Am Under Custody",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));
        questions.add(new Question("37", "Are You Getting Any Mental Help Nowadays? If You Are, By Whom and When Was Your Last Appointment? (Psychiatrist/Social Worker/Psychologist)",
                QuestionTypeEnum.Open, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("38", "Rank Your Distress Level on a Scale of 1 to 10:",
                QuestionTypeEnum.Slider, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("39", "Rank Your Daily Functioning on a Scale of 1 to 10:",
                QuestionTypeEnum.Slider, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("4", "Do You Have Anyone Around to Support You?",
                QuestionTypeEnum.Open, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("40", "Does Anyone Take Advantage of You or Hurts You?",
                QuestionTypeEnum.Open, false, ViewModelEnum.SocialQuestions));

        questions.add(new Question("41", "What is Your Height? (Cm)",
                QuestionTypeEnum.Number, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("42", "What is Your Weight? (Kg)",
                QuestionTypeEnum.Number, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("43", "Were There Any Recent Changes In Your Weight, Diet or Physical Activity?",
                QuestionTypeEnum.Open, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("44", "Has Your Appetite been Decreased or Increased?",
                QuestionTypeEnum.Open, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("45", "Do You Have Any Sleeping Issues?",
                QuestionTypeEnum.Open, false, ViewModelEnum.HabitsQuestions));

        questions.add(new Question("46", "I Experience Bad Mood",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("47", "I Experience Uplifting Mood",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("48", "I Experience Anxiety",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("49", "I Experience Suspiciousness",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("5", "I Experience Stress",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("50", "I Have Peculiar Thoughts",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("51", "I Experience Strange Sensation",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("52", "I Experience Anger",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("53", "I Have Aggressive Thoughts",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("54", "I Have Suicidal Thoughts",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("55", "I Experience Concentration Issues",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("56", "I Experience Memory Issues",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("57", "I Experience Decrease in My Vision",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("58", "I Experience Decrease in My Hearing",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("59", "I Experience Walking Difficulties",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));

        for (Question question : questions) {
            mCloudDB.collection(QUESTIONS)
                    .document("en")
                    .collection(QUESTIONS_LOCAL)
                    .document(question.getId())
                    .set(question);
        }
    }

    public void addQuestionsHebrew() {
        final List<Question> questions = new ArrayList<>();

        questions.add(new Question("1", "אעדיף פגישה ארוכה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));
        questions.add(new Question("10", "אעדיף פגישה קצרה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));
        questions.add(new Question("11", "אעדיף שאדם נוסף ילווה אותי במהלך הפגישה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Treaty));

        questions.add(new Question("12", "טופס 17",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("13", "הארכה לטופס 17",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("14", "סיכום פגישה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("15", "מרשמים",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("16", "מכתב לביטוח לאומי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("17", "מכתב לרופא משפחה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("18", "מסמך לוועדת סל שיקום",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("19", "מכתב לרופא תעסוקתי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("2", "בקשה לטיפול פסיכולוגי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("20", "מסמך לעובד סוציאלי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));
        questions.add(new Question("21", "אחר",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Bureaucracy));

        questions.add(new Question("22", "יש לי אחד מהתסמינים הבאים - חום גבוה, כאב גרון, אובדן חוש טעם, אובדן חודש ריח, שיעול, כאבי שרירים. ",
                QuestionTypeEnum.Binary, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("23", "אני מאובחן כיום כחולה קורונה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("24", "מישהו מבני ביתי כחולה כיום בקורונה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("25", "אם אחד מקרובי משפחתך חלה בקורונה?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("26", "האם חלית בקורונה בעבר ואם כן מתי?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("27", "האם בשל מגפת הקורונה נגרם לך סבל נפשי?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("28", "האם בשל מחלת הקורונה נגרם לך סבל גופני?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("29", "האם בשל מגפת הקורונה (גם אם לא חלית בעצמך) - נגרם לך סבל נפשי?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));
        questions.add(new Question("3", "האם בשל מגפת הקורונה (גם אם לא חלית בעצמך) - נגרם לך סבל גופני?",
                QuestionTypeEnum.Open, false, ViewModelEnum.CovidQuestions));

        questions.add(new Question("30", "אני מרגיש/ה אובדני/ת",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));
        questions.add(new Question("31", "אני מרגיש/ה תוקפני/ת",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));
        questions.add(new Question("32", "אני שומע/ת קולות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));
        questions.add(new Question("33", "אני מרגיש/ה כאב חזק",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SanityCheck));

        questions.add(new Question("34", "עברתי תאונה לאחרונה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("35", "אושפזתי לאחרונה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("350", "חל שינוי במינון התרופות שלי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("351", "הפסקתי ליטול תרופה כלשהי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("352", "יש לי אבחון רפואי חדש",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("353", "עברתי חבלה גופנית לאחרונה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("354", "אני נמצא כעת בהליך משפטי",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));
        questions.add(new Question("355", "פניתי למיון לאחרונה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.Statement));

        questions.add(new Question("36", "יש עליי אפוטרופוסות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("37", "האם יש לך כיום מטפלים בתחום בריאות הנפש ואם כן מי הם ומתי נבדקת אצלם לאחרונה? (פסיכיאטר/עו\"ס/פסיכולוג)",
                QuestionTypeEnum.Open, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("38", "דרג/י את עוצמת מצוקתך מ-1 עד 10:",
                QuestionTypeEnum.Slider, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("39", "דרג/י את רמת התפקוד היומיומי שלך מ-1 עד 10:",
                QuestionTypeEnum.Slider, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("4", "האם יש מישהו תומך בסביבתך?",
                QuestionTypeEnum.Open, false, ViewModelEnum.SocialQuestions));
        questions.add(new Question("40", "האם מישהו מנצל אותך? פוגע בך?",
                QuestionTypeEnum.Open, false, ViewModelEnum.SocialQuestions));

        questions.add(new Question("41", "מה הגובה שלך? (ס\"מ)",
                QuestionTypeEnum.Number, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("42", "מה המשקל שלך? (ק\"ג)",
                QuestionTypeEnum.Number, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("43", "האם חלו לאחרונה שינויים במשקל, בתזונה או פעילות גופנית?",
                QuestionTypeEnum.Open, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("44", "האם התאבון שלך ירד? עלה?",
                QuestionTypeEnum.Open, false, ViewModelEnum.HabitsQuestions));
        questions.add(new Question("45", "האם השינה שלך פגומה?",
                QuestionTypeEnum.Open, false, ViewModelEnum.HabitsQuestions));

        questions.add(new Question("46", "אני מרגיש/ה מצב רוח ירוד",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("47", "אני מרגיש/ה מצב רוב מרומם",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("48", "אני מרגיש/ה חרדה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("49", "אני מרגיש/ה חשדנות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("5", "אני מרגיש/ה מתח",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("50", "יש לי מחשבות מוזרות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("51", "יש לי תחושות מוזרות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("52", "אני מרגיש/ה כעס",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("53", "יש לי מחשבות תוקפניות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("54", "יש לי מחשבות אובדניות",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("55", "יש לי פגיעה בריכוז",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("56", "יש לי פגיעה בזיכרון",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("57", "יש לי ירידה בראיה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("58", "יש לי ירידה בשמיעה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));
        questions.add(new Question("59", "יש לי קושי בהליכה",
                QuestionTypeEnum.Binary, false, ViewModelEnum.MentalQuestions));

        for (Question question : questions) {
            mCloudDB.collection(QUESTIONS)
                    .document("he")
                    .collection(QUESTIONS_LOCAL)
                    .document(question.getId())
                    .set(question);
        }
    }

    public void addFeelingsEnglish() {
        final List<Feeling> feelings = new ArrayList<>();

        feelings.add(new Feeling("1", R.drawable.fear, "Fear"));
        feelings.add(new Feeling("10", R.drawable.tension, "Tension"));
        feelings.add(new Feeling("11", R.drawable.happiness, "Happiness"));
        feelings.add(new Feeling("2", R.drawable.sadness, "Sadness"));
        feelings.add(new Feeling("3", R.drawable.anger, "Anger"));
        feelings.add(new Feeling("4", R.drawable.anxiety, "Anxiety"));
        feelings.add(new Feeling("40", R.drawable.uplift, "Uplift"));
        feelings.add(new Feeling("5", R.drawable.depression, "Depression"));
        feelings.add(new Feeling("6", R.drawable.disturbed, "Disturbed"));
        feelings.add(new Feeling("7", R.drawable.embarrassment, "Embarrassment"));
        feelings.add(new Feeling("70", R.drawable.peace, "Peace"));
        feelings.add(new Feeling("8", R.drawable.confusion, "Confusion"));
        feelings.add(new Feeling("9", R.drawable.aggressive, "Aggressive"));

        for (Feeling feeling : feelings) {
            mCloudDB.collection(FEELINGS)
                    .document("en")
                    .collection(FEELINGS_LOCAL)
                    .document(feeling.getId())
                    .set(feeling);
        }
    }

    public void addFeelingsHebrew() {
        final List<Feeling> feelings = new ArrayList<>();

        feelings.add(new Feeling("1", R.drawable.fear, "פחד"));
        feelings.add(new Feeling("10", R.drawable.tension, "מתח"));
        feelings.add(new Feeling("11", R.drawable.happiness, "שמחה"));
        feelings.add(new Feeling("2", R.drawable.sadness, "עצב"));
        feelings.add(new Feeling("3", R.drawable.anger, "כעס"));
        feelings.add(new Feeling("4", R.drawable.anxiety, "חרדה"));
        feelings.add(new Feeling("40", R.drawable.uplift, "מרומם"));
        feelings.add(new Feeling("5", R.drawable.depression, "דיכאון"));
        feelings.add(new Feeling("6", R.drawable.disturbed, "מוטרד"));
        feelings.add(new Feeling("7", R.drawable.embarrassment, "מבוכה"));
        feelings.add(new Feeling("70", R.drawable.peace, "שלווה"));
        feelings.add(new Feeling("8", R.drawable.confusion, "בלבול"));
        feelings.add(new Feeling("9", R.drawable.aggressive, "תוקפנות"));

        for (Feeling feeling : feelings) {
            mCloudDB.collection(FEELINGS)
                    .document("he")
                    .collection(FEELINGS_LOCAL)
                    .document(feeling.getId())
                    .set(feeling);
        }
    }
}
