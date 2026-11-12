package com.oidioomstudio.olib.mixin;

import com.oidioomstudio.olib.api.i18n.OLanguageManager;
import net.minecraft.client.resources.language.ClientLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientLanguage.class)
public class ClientLanguageMixin {

    @Inject(method = "getOrDefault(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;", at = @At("HEAD"), cancellable = true)
    private void olib$getOrDefault(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        String custom = OLanguageManager.get(key);
        if (custom != null) {
            cir.setReturnValue(custom);
        }
    }

    @Inject(method = "has", at = @At("HEAD"), cancellable = true)
    private void olib$has(String key, CallbackInfoReturnable<Boolean> cir) {
        if (OLanguageManager.has(key)) {
            cir.setReturnValue(true);
        }
    }
}
