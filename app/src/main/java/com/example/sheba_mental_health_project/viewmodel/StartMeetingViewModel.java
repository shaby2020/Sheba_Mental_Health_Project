package com.example.sheba_mental_health_project.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.sheba_mental_health_project.model.Appointment;
import com.example.sheba_mental_health_project.model.enums.AppointmentStateEnum;
import com.example.sheba_mental_health_project.repository.Repository;

public class StartMeetingViewModel extends ViewModel {

    final private Repository mRepository;

    private MutableLiveData<Appointment> mGetLastAppointmentSucceed;
    private MutableLiveData<String> mGetLastAppointmentFailed;

    private  MutableLiveData<AppointmentStateEnum> mGetUpdateStateSucceed;
    private  MutableLiveData<String> mGetUpdateStateFailed;

    public StartMeetingViewModel(final Context context) {
        mRepository = Repository.getInstance(context);
    }

    public MutableLiveData<Appointment> getGetLastAppointmentSucceed() {
        if (mGetLastAppointmentSucceed == null) {
            mGetLastAppointmentSucceed = new MutableLiveData<>();
            attachSetGetLastAppointmentListener();
        }
        return mGetLastAppointmentSucceed;
    }

    public MutableLiveData<String> getGetLastAppointmentFailed() {
        if (mGetLastAppointmentFailed == null) {
            mGetLastAppointmentFailed = new MutableLiveData<>();
            attachSetGetLastAppointmentListener();
        }
        return mGetLastAppointmentFailed;
    }

    private void attachSetGetLastAppointmentListener() {
        mRepository.setGetLastAppointmentInterface(new Repository.RepositoryGetLastAppointmentInterface() {
            @Override
            public void onGetLastAppointmentSucceed(Appointment lastAppointment) {
                mGetLastAppointmentSucceed.setValue(lastAppointment);
            }

            @Override
            public void onGetLastAppointmentFailed(String error) {
                mGetLastAppointmentFailed.setValue(error);
            }
        });
    }

    public MutableLiveData<AppointmentStateEnum> getGetUpdateStateSucceed() {
        if (mGetUpdateStateSucceed == null) {
            mGetUpdateStateSucceed = new MutableLiveData<>();
            attachSetUpdateStateListener();
        }
        return mGetUpdateStateSucceed;
    }



    public MutableLiveData<String> getGetUpdateStateFailed() {
        if (mGetUpdateStateFailed == null) {
            mGetUpdateStateFailed = new MutableLiveData<>();
            attachSetUpdateStateListener();
        }
        return mGetUpdateStateFailed;
    }

    private void attachSetUpdateStateListener() {
        mRepository.setUpdateStateOfAppointmentInterface(new Repository.RepositoryUpdateStateOfAppointmentInterface() {
            @Override
            public void onUpdateStateOfAppointmentSucceed(AppointmentStateEnum appointmentState) {
                mGetUpdateStateSucceed.setValue(appointmentState);
            }

            @Override
            public void onUpdateAnswersOfAppointmentFailed(String error) {
                mGetUpdateStateFailed.setValue(error);
            }
        });
    }

    public void getLastMeeting() {
        mRepository.getLastAppointment();
    }

    public void updateState(AppointmentStateEnum stateEnum) {
        mRepository.updateAppointmentState(stateEnum);
    }
}