package com.example.sheba_mental_health_project.view;

import androidx.fragment.app.FragmentContainerView;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.sheba_mental_health_project.R;
import com.example.sheba_mental_health_project.model.Appointment;
import com.example.sheba_mental_health_project.model.ViewModelFactory;
import com.example.sheba_mental_health_project.model.enums.AppointmentStateEnum;
import com.example.sheba_mental_health_project.model.enums.BodyPartEnum;
import com.example.sheba_mental_health_project.model.enums.ViewModelEnum;
import com.example.sheba_mental_health_project.viewmodel.StartMeetingViewModel;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;

public class StartMeetingFragment extends Fragment {

    private final String TAG = "StartMeetingFragment";

    private StartMeetingViewModel mViewModel;

    private TextView mDateTv;
    private TextView mTherapistNameTv;
    private TextView mPatientNameTv;
    private TextView mNoMeetingTv;
    private RelativeLayout mMainLayout;

    public interface StartMeetingTherapistInterface {
        void onTherapistStartMeetingClicked();
    }

    private StartMeetingTherapistInterface listener;

    public static StartMeetingFragment newInstance() {
        return new StartMeetingFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (StartMeetingTherapistInterface) context;
        } catch (Exception ex) {
            throw new ClassCastException("The Activity Must Implements StartMeetingTherapistInterface listener!");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this,new ViewModelFactory(getContext(),
                ViewModelEnum.StartMeeting)).get(StartMeetingViewModel.class);

        Observer<Appointment> appointmentSucceedObserver = new Observer<Appointment>() {
            @Override
            public void onChanged(Appointment appointment) {
                final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm | dd/MM");

                mPatientNameTv.setText(appointment.getPatient().getFullName());
                mTherapistNameTv.setText(appointment.getTherapist().getFullName());
                mDateTv.setText(simpleDateFormat.format(appointment.getAppointmentDate()));
                mMainLayout.setVisibility(View.VISIBLE);
                mNoMeetingTv.setVisibility(View.GONE);

                getChildFragmentManager().beginTransaction()
                        .add(R.id.character_container, CharacterFragment.newInstance(appointment, false))
                        .commit();
            }
        };

        Observer<String> appointmentFailedObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e(TAG, "onChanged: "+s);
                mMainLayout.setVisibility(View.GONE);
                mNoMeetingTv.setVisibility(View.VISIBLE);
            }
        };

        Observer<AppointmentStateEnum> appointmentStateSucceedObserver = new Observer<AppointmentStateEnum>() {
            @Override
            public void onChanged(AppointmentStateEnum appointmentStateEnum) {
                //TODO send notification to patient and move to next fragment
            }
        };

        Observer<String> appointmentStateFailedObserver = new Observer<String>() {
            @Override
            public void onChanged(String s) {
                Log.e(TAG, "onChanged: "+s );
            }
        };

        mViewModel.getGetLastAppointmentSucceed().observe(this,appointmentSucceedObserver);
        mViewModel.getGetLastAppointmentFailed().observe(this,appointmentFailedObserver);
        mViewModel.getGetUpdateStateSucceed().observe(this,appointmentStateSucceedObserver);
        mViewModel.getGetUpdateStateFailed().observe(this,appointmentStateFailedObserver);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.start_meeting_fragment, container, false);

        mDateTv = rootView.findViewById(R.id.last_meeting_date_tv);
        mTherapistNameTv = rootView.findViewById(R.id.last_therapist_name_tv);
        mPatientNameTv = rootView.findViewById(R.id.patient_name_tv);
        mNoMeetingTv = rootView.findViewById(R.id.no_meeting_tv);
        mMainLayout = rootView.findViewById(R.id.start_meeting_relative);
        final MaterialButton startMeetingBtn = rootView.findViewById(R.id.start_meeting_btn);

        startMeetingBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mViewModel.updateState(AppointmentStateEnum.Ongoing);
            }
        });

        mViewModel.getLastMeeting();
        return  rootView;
    }
}
