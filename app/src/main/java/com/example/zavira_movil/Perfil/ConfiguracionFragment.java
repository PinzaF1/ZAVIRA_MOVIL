package com.example.zavira_movil.Perfil;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.zavira_movil.BasicResponse;
import com.example.zavira_movil.LoginActivity;
import com.example.zavira_movil.R;
import com.example.zavira_movil.model.CambiarPassword;
import com.example.zavira_movil.model.Estudiante;
import com.example.zavira_movil.model.LoginRequest;
import com.example.zavira_movil.remote.ApiService;
import com.example.zavira_movil.remote.RetrofitClient;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ConfiguracionFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_menu_configuracion, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        View rowCambiar = view.findViewById(R.id.btnCambiarContrasena);        if (rowCambiar != null) rowCambiar.setOnClickListener(v -> mostrarDialogoCambio());

        View rowLogout = view.findViewById(R.id.btnCerrarSesion);
        if (rowLogout != null) rowLogout.setOnClickListener(v -> confirmarCerrarSesion());
    }

    // ---------------------------------------------------------------------
    // Helpers UI
    // ---------------------------------------------------------------------
    private Dialog showLoading() {
        Dialog d = new Dialog(requireContext());
        d.requestWindowFeature(Window.FEATURE_NO_TITLE);
        d.setContentView(new ProgressBar(requireContext()));
        d.setCancelable(false);
        d.show();
        return d;
    }

    private void showLong(String msg) {
        Toast.makeText(requireContext(), msg, Toast.LENGTH_LONG).show();
    }

    // ---------------------------------------------------------------------
    // Cambiar contraseña
    // ---------------------------------------------------------------------
    private void mostrarDialogoCambio() {
        // Inflar el layout personalizado
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_cambiar_contrasena_inicial, null);

        // Crear el diálogo
        androidx.appcompat.app.AlertDialog dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setCancelable(true)
                .create();

        // Configurar fondo transparente para respetar las esquinas redondeadas
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        // Configurar los botones
        MaterialButton btnSiRecuerdo = dialogView.findViewById(R.id.btnSiRecuerdo);
        MaterialButton btnCancelar = dialogView.findViewById(R.id.btnCancelar);
        MaterialButton btnNoRecuerdo = dialogView.findViewById(R.id.btnNoRecuerdo);

        btnSiRecuerdo.setOnClickListener(v -> {
            mostrarDialogoCambioNormal();
            dialog.dismiss();
        });

        btnCancelar.setOnClickListener(v -> {
            dialog.dismiss();
        });

        btnNoRecuerdo.setOnClickListener(v -> {
            Intent intent = new Intent(requireActivity(), com.example.zavira_movil.resetpassword.ResetPasswordActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Diálogo normal para cambiar contraseña (requiere contraseña actual)
     */
    private void mostrarDialogoCambioNormal() {
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialogo_cambiar_contrasena, null, false);

        TextInputEditText etActual = content.findViewById(R.id.etActual);
        TextInputEditText etNueva = content.findViewById(R.id.etNueva);
        TextInputEditText etConfirmar = content.findViewById(R.id.etConfirmar);

        // Botones del layout
        MaterialButton btnCancelar = content.findViewById(R.id.btnCancelar);
        MaterialButton btnGuardar = content.findViewById(R.id.btnGuardar);

        // Placeholders que se quitan al enfocar y vuelven si queda vacío
        final String H1 = "Ingresa tu contraseña actual";
        final String H2 = "Mínimo 8 caracteres";
        final String H3 = "Repite la nueva contraseña";

        etActual.setHint(H1);
        etNueva.setHint(H2);
        etConfirmar.setHint(H3);

        View.OnFocusChangeListener clearOnFocus = (v, hasFocus) -> {
            TextInputEditText et = (TextInputEditText) v;
            if (hasFocus) {
                et.setHint("");
            } else {
                boolean empty = et.getText() == null || et.getText().length() == 0;
                if (empty) {
                    if (et == etActual) et.setHint(H1);
                    else if (et == etNueva) et.setHint(H2);
                    else if (et == etConfirmar) et.setHint(H3);
                }
            }
        };
        etActual.setOnFocusChangeListener(clearOnFocus);
        etNueva.setOnFocusChangeListener(clearOnFocus);
        etConfirmar.setOnFocusChangeListener(clearOnFocus);

        final var dialog = new MaterialAlertDialogBuilder(requireContext())
                // El título ya lo tienes en el XML (TextView superior)
                .setView(content)
                .create();

        // Acciones
        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnGuardar.setOnClickListener(v -> {
            String actual = etActual.getText() != null ? etActual.getText().toString().trim() : "";
            String nueva = etNueva.getText() != null ? etNueva.getText().toString().trim() : "";
            String conf = etConfirmar.getText() != null ? etConfirmar.getText().toString().trim() : "";

            // Validaciones
            if (TextUtils.isEmpty(actual) || TextUtils.isEmpty(nueva) || TextUtils.isEmpty(conf)) {
                Toast.makeText(requireContext(), "Completa todos los campos", Toast.LENGTH_SHORT).show();
                return;
            }
            if (nueva.length() < 6) {
                etNueva.setError("Mínimo 6 caracteres");
                return;
            }
            if (!nueva.equals(conf)) {
                etConfirmar.setError("No coincide");
                return;
            }

            Dialog loading = showLoading();

            ApiService api = RetrofitClient
                    .getInstance()
                    .create(ApiService.class);

            CambiarPassword body = new CambiarPassword(actual, nueva);

            api.cambiarPasswordMovil(body).enqueue(new Callback<>() {
                @Override
                public void onResponse(@NonNull Call<BasicResponse> call, @NonNull Response<BasicResponse> resp) {
                    if (loading.isShowing()) loading.dismiss();

                    if (resp.isSuccessful() && resp.body() != null && resp.body().isOk()) {
                        // Verificar login con la nueva clave
                        verificarLoginConNuevaClave(api, nueva, dialog);
                    } else {
                        String msg = "Error " + resp.code();
                        try {
                            if (resp.errorBody() != null) msg += ": " + resp.errorBody().string();
                        } catch (Exception ignored) {
                        }
                        showLong(msg);
                    }
                }

                @Override
                public void onFailure(@NonNull Call<BasicResponse> call, @NonNull Throwable t) {
                    if (loading.isShowing()) loading.dismiss();
                    showLong("Fallo de red: " + t.getMessage());
                }
            });
        });

        dialog.show();
    }

    /**
     * Llama al perfil para obtener numero_documento y prueba login con la nueva contraseña.
     */
    private void verificarLoginConNuevaClave(ApiService api, String nuevaClave, Dialog dialogCambio) {
        api.getPerfilEstudiante().enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<Estudiante> call, @NonNull Response<Estudiante> rPerfil) {
                if (!rPerfil.isSuccessful() || rPerfil.body() == null) {
                    showLong("Contraseña actualizada. No pude leer el perfil para verificar login.");
                    dialogCambio.dismiss();
                    return;
                }

                String numeroDoc = null;
                try {
                    numeroDoc = rPerfil.body().getNumeroDocumento();
                } catch (Exception ignored) {
                }

                if (numeroDoc == null || numeroDoc.trim().isEmpty()) {
                    showLong("Contraseña actualizada. No pude obtener el documento del perfil.");
                    dialogCambio.dismiss();
                    return;
                }

                LoginRequest loginReq = new LoginRequest(numeroDoc.trim(), nuevaClave);

                api.loginEstudiante(loginReq).enqueue(new Callback<>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> rLogin) {
                        if (rLogin.isSuccessful()) {
                            showLong("Contraseña actualizada correctamente");
                            dialogCambio.dismiss();
                        } else {
                            String msg = "Falló al actualizar la contraseña (" + rLogin.code() + ")";
                            try {
                                if (rLogin.errorBody() != null)
                                    msg += ": " + rLogin.errorBody().string();
                            } catch (Exception ignored) {
                            }
                            showLong(msg + " → Revisa persistencia/hash en el backend.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        showLong("No pude verificar login: " + t.getMessage());
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<Estudiante> call, @NonNull Throwable t) {
                showLong("Contraseña actualizada. No pude leer el perfil: " + t.getMessage());
                dialogCambio.dismiss();
            }
        });
    }

    // ---------------------------------------------------------------------
    // Cerrar sesión
    // ---------------------------------------------------------------------
    private void confirmarCerrarSesion() {
        // Infla el layout personalizado
        View content = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_cerrar_sesion, null, false);

        final var dialog = new MaterialAlertDialogBuilder(requireContext())
                .setView(content)
                .create();

        // Fondo transparente para respetar el card redondeado
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(
                    new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT)
            );
        }

        // Botones
        MaterialButton btnCancelar = content.findViewById(R.id.btnCancelarCerrar);
        MaterialButton btnConfirmar = content.findViewById(R.id.btnConfirmarCerrar);

        btnCancelar.setOnClickListener(v -> dialog.dismiss());

        btnConfirmar.setOnClickListener(v -> {
            // Acción real de cerrar sesión
            logoutWithUnregister(dialog);
        });

        dialog.show();
    }

    /**
     * Intenta desregistrar el token FCM en el servidor y luego realiza el logout local.
     * Siempre que falle la petición, igual se realiza el logout local (comportamiento "best-effort").
     */
    private void logoutWithUnregister(android.app.Dialog confirmDialog) {
        // Mostrar loading
        Dialog loading = showLoading();

        // Obtener token FCM guardado
        String fcmToken = new com.example.zavira_movil.notifications.NotificationHelper(requireContext()).getSavedToken();
        if (fcmToken == null) {
            // No hay token FCM: finalizar logout inmediato
            if (loading.isShowing()) loading.dismiss();
            com.example.zavira_movil.local.TokenManager.clearAll(requireContext());
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
            confirmDialog.dismiss();
            return;
        }

        try {
            String deviceId = android.provider.Settings.Secure.getString(
                    requireContext().getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );

            org.json.JSONObject jsonBody = new org.json.JSONObject();
            jsonBody.put("token", fcmToken);
            jsonBody.put("device_id", deviceId);
            jsonBody.put("platform", "android");

            okhttp3.RequestBody body = okhttp3.RequestBody.create(
                    jsonBody.toString(),
                    okhttp3.MediaType.parse("application/json")
            );

            ApiService api = RetrofitClient.getInstance().create(ApiService.class);
            api.unregisterFCMToken(body).enqueue(new retrofit2.Callback<>() {
                @Override
                public void onResponse(@NonNull retrofit2.Call<Void> call, @NonNull retrofit2.Response<Void> response) {
                    // Ignorar éxito/fallo y finalizar logout
                    if (loading.isShowing()) loading.dismiss();
                    com.example.zavira_movil.local.TokenManager.clearAll(requireContext());
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    requireActivity().finish();
                    confirmDialog.dismiss();
                }

                @Override
                public void onFailure(@NonNull retrofit2.Call<Void> call, @NonNull Throwable t) {
                    // Best-effort: igualmente cerrar sesión localmente
                    if (loading.isShowing()) loading.dismiss();
                    com.example.zavira_movil.local.TokenManager.clearAll(requireContext());
                    Intent i = new Intent(requireContext(), LoginActivity.class);
                    i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(i);
                    requireActivity().finish();
                    confirmDialog.dismiss();
                }
            });
        } catch (Exception e) {
            if (loading.isShowing()) loading.dismiss();
            com.example.zavira_movil.local.TokenManager.clearAll(requireContext());
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(i);
            requireActivity().finish();
            confirmDialog.dismiss();
        }
    }
}

