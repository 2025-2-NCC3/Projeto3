package com.example.pi.viewmodels;


import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.example.pi.models.User;
import com.example.pi.repositories.UserRepository;
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.disposables.CompositeDisposable;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class RegisterViewModel extends ViewModel {

    private UserRepository userRepository;
    private final CompositeDisposable disposables = new CompositeDisposable();

    // LiveData para expor o resultado para a UI
    private final MutableLiveData<User> userRegistrationSuccess = new MutableLiveData<>();
    private final MutableLiveData<String> userRegistrationError = new MutableLiveData<>();

    public RegisterViewModel() {
        this.userRepository = new UserRepository();
    }

    public LiveData<User> getUserRegistrationSuccess() {
        return userRegistrationSuccess;
    }

    public LiveData<String> getUserRegistrationError() {
        return userRegistrationError;
    }

    public void registerNewUser(String nome, String email, String senha) {
        // Adicionamos a operação ao CompositeDisposable para podermos cancelá-la se o ViewModel for destruído
        disposables.add(userRepository.registerUser(nome, email, senha)
                // IMPORTANTE: A chamada de rede deve ser feita em uma background thread (io)
                .subscribeOn(Schedulers.io())
                // O resultado deve ser observado na thread principal (UI thread)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                        // Em caso de sucesso
                        user -> userRegistrationSuccess.setValue(user),
                        // Em caso de erro
                        error -> userRegistrationError.setValue("Erro ao registrar: " + error.getMessage())
                ));
    }

    @Override
    protected void onCleared() {
        // Limpa todas as subscrições RxJava para evitar memory leaks quando o ViewModel for destruído
        super.onCleared();
        disposables.clear();
    }}