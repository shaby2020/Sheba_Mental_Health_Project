package com.example.sheba_mental_health_project.view.patient;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import android.content.Context;
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
import com.example.sheba_mental_health_project.model.Question;
import com.example.sheba_mental_health_project.model.QuestionsAdapter;
import com.example.sheba_mental_health_project.model.ViewModelFactory;
import com.example.sheba_mental_health_project.model.enums.ViewModelEnum;
import com.example.sheba_mental_health_project.view.ConfirmationDialog;
import com.example.sheba_mental_health_project.viewmodel.AnxietyQuestionsViewModel;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class AnxietyQuestionsFragment extends Fragment {

    private AnxietyQuestionsViewModel mViewModel;

    private RecyclerView mRecyclerView;

    private QuestionsAdapter mQuestionsAdapter;

    private final String TAG = "AnxietyQuestionsFrag";


    public interface AnxietyQuestionsFragmentInterface {
        void onContinueFromAnxietyQuestions();
    }

    private AnxietyQuestionsFragmentInterface listener;

    public static AnxietyQuestionsFragment newInstance() {
        return new AnxietyQuestionsFragment();
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);

        try {
            listener = (AnxietyQuestionsFragmentInterface) context;
        } catch (Exception ex) {
            throw new ClassCastException("The Activity Must Implements AnxietyQuestionsFragmentInterface listener!");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mViewModel = new ViewModelProvider(this, new ViewModelFactory(getContext(),
                ViewModelEnum.AnxietyQuestions)).get(AnxietyQuestionsViewModel.class);

        final Observer<List<Question>> onGetQuestionsOfPageSucceed = new Observer<List<Question>>() {
            @Override
            public void onChanged(List<Question> questions) {
                mQuestionsAdapter = new QuestionsAdapter(getContext(), questions,
                        mViewModel.getCurrentAppointment().getAnswers());
                mRecyclerView.setAdapter(mQuestionsAdapter);
            }
        };

        final Observer<String> onGetQuestionsOfPageFailed = new Observer<String>() {
            @Override
            public void onChanged(String error) {
                Log.w(TAG, "onChanged: " + error);
            }
        };

        final Observer<Appointment> onUpdateAnswersOfAppointmentSucceed = new Observer<Appointment>() {
            @Override
            public void onChanged(Appointment appointment) {
                if (listener != null) {
                    listener.onContinueFromAnxietyQuestions();
                }
            }
        };

        final Observer<String> onUpdateAnswersOfAppointmentFailed = new Observer<String>() {
            @Override
            public void onChanged(String error) {
                Log.e(TAG, "onChanged: " + error);
            }
        };

        mViewModel.getGetQuestionsOfPageSucceed().observe(this, onGetQuestionsOfPageSucceed);
        mViewModel.getGetQuestionsOfPageFailed().observe(this, onGetQuestionsOfPageFailed);
        mViewModel.getUpdateAnswersOfAppointmentSucceed().observe(this, onUpdateAnswersOfAppointmentSucceed);
        mViewModel.getUpdateAnswersOfAppointmentFailed().observe(this, onUpdateAnswersOfAppointmentFailed);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.anxiety_questions_fragment, container, false);

        mViewModel.attachSetGetQuestionsOfPageListener();
        mViewModel.attachSetUpdateAnswersOfAppointmentListener();

        mRecyclerView = rootView.findViewById(R.id.questions_recycler);
        final MaterialButton backBtn = rootView.findViewById(R.id.back_btn);
        final MaterialButton continueBtn = rootView.findViewById(R.id.continue_btn);

        mRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecyclerView.setHasFixedSize(true);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().onBackPressed();
            }
        });

        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: Add loading dialog
                if (mQuestionsAdapter.isAllMandatoryQuestionsFilled()) {
                    mViewModel.updateAnswersOfAppointment();
                } else {
                    final ConfirmationDialog dialog = new ConfirmationDialog(requireContext());
                    dialog.setPromptText(getString(R.string.mandatory_questions_warning));
                    dialog.setOnActionListener(new ConfirmationDialog.ConfirmationDialogActionInterface() {
                        @Override
                        public void onOkBtnClicked() {}
                    });
                    dialog.show();
                }
            }
        });

        mViewModel.getQuestions(ViewModelEnum.AnxietyQuestions);

        return rootView;
    }
}