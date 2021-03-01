package com.example.sheba_mental_health_project.view;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.sheba_mental_health_project.R;
import com.example.sheba_mental_health_project.model.Appointment;
import com.example.sheba_mental_health_project.model.PainPoint;
import com.example.sheba_mental_health_project.model.TherapistAppointmentsAdapter;
import com.example.sheba_mental_health_project.model.TherapistPhysicalStateAdapter;
import com.example.sheba_mental_health_project.model.ViewModelFactory;
import com.example.sheba_mental_health_project.model.enums.PainLocationEnum;
import com.example.sheba_mental_health_project.model.enums.PainTypeEnum;
import com.example.sheba_mental_health_project.model.enums.ViewModelEnum;
import com.example.sheba_mental_health_project.viewmodel.MainTherapistViewModel;
import com.example.sheba_mental_health_project.viewmodel.TherapistPhysicalStateViewModel;

import java.util.List;

public class TherapistPhysicalStateFragment extends Fragment {

    private TherapistPhysicalStateViewModel mViewModel;

    private RecyclerView mRecyclerView;
    private TherapistPhysicalStateAdapter mTherapistPhysicalStateAdapter;

    private final String TAG = "TherapistFragment";


    public static TherapistPhysicalStateFragment newInstance() {
        return new TherapistPhysicalStateFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new ViewModelFactory(getContext(),
                ViewModelEnum.TherapistPhysicalState)).get(TherapistPhysicalStateViewModel.class);

        final Observer<List <PainPoint>> onGetTherapistPhysicalStateSucceed = new Observer<List<PainPoint>>() {
            @Override
            public void onChanged(List<PainPoint> painPoints) {
                for (int i = 0; i < painPoints.size(); i++) {
                    if (painPoints.get(i).getPainLocation() == PainLocationEnum.Mouth) {
                        painPoints.remove(painPoints.get(i));
                    }
                }

                mTherapistPhysicalStateAdapter = new TherapistPhysicalStateAdapter(requireContext(), painPoints);
                mRecyclerView.setAdapter(mTherapistPhysicalStateAdapter);
            }
        };

        final Observer<String> onGetTherapistPhysicalFailed = new Observer<String>() {
            @Override
            public void onChanged(String error) {
                Log.d(TAG, "onChanged: " + error);
            }
        };

        mViewModel.getTherapistPhysicalStatePainPointsSucceed().observe(this,onGetTherapistPhysicalStateSucceed);
        mViewModel.getTherapistPhysicalStatePainPointsFailed().observe(this,onGetTherapistPhysicalFailed);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        final View rootView = inflater.inflate(R.layout.therapist_physical_state_fragment, container, false);
        mRecyclerView = rootView.findViewById(R.id.recycler_view);

        //TODO: Show some text if the appointments list is empty.

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);

        mViewModel.getPainPoints(mViewModel.getCurrentAppointment());

        return rootView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        mViewModel.removeGetAllPainPointsListener();
    }
}