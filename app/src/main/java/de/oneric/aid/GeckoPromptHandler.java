/*
    This file is part of AiD.

    AiD is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    AiD is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with AiD.  If not, see <http://www.gnu.org/licenses/>.

    Copyright 2020  Oneric
*/
package de.oneric.aid;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.util.Log;

import org.mozilla.geckoview.Autocomplete;
import org.mozilla.geckoview.Autocomplete.LoginSaveOption;
import org.mozilla.geckoview.Autocomplete.LoginSelectOption;
import org.mozilla.geckoview.GeckoResult;
import org.mozilla.geckoview.GeckoSession;

import java.util.ArrayList;
import java.util.List;


public class GeckoPromptHandler implements GeckoSession.PromptDelegate {

    private static final String TAG = "core/prompt";
    private final GeckoActivity mainActivity;

    public GeckoPromptHandler(final GeckoActivity a) {
        mainActivity = a;
    }

    private AlertDialog createStandardDialog(final AlertDialog.Builder builder,
                                             final BasePrompt prompt,
                                             final GeckoResult<PromptResponse> response) {
        final AlertDialog dialog = builder.create();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                if (!prompt.isComplete()) {
                    response.complete(prompt.dismiss());
                }
            }
        });
        return dialog;
    }

    @Override
    public GeckoResult<PromptResponse> onAlertPrompt(final GeckoSession session,
                                                     final AlertPrompt prompt) {
        final Activity activity = mainActivity;
        if (activity == null) {
            return GeckoResult.fromValue(prompt.dismiss());
        }
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity)
                .setTitle(prompt.title)
                .setMessage(prompt.message)
                .setPositiveButton(android.R.string.ok, /* onClickListener */ null);
        GeckoResult<PromptResponse> res = new GeckoResult<>();
        createStandardDialog(builder, prompt, res).show();
        return res;
    }

    @Override
    public GeckoResult<PromptResponse> onLoginSave(final GeckoSession session,
                                                   final AutocompleteRequest<LoginSaveOption> request) {

        GeckoResult<PromptResponse> res = new GeckoResult<>();
        res.complete(request.confirm(new Autocomplete.LoginSelectOption(request.options[0].value)));
        return res;
    }

    @Override
    public GeckoResult<PromptResponse> onLoginSelect(final GeckoSession session,
                                                     final AutocompleteRequest<LoginSelectOption> request) {

        if(!session.getAutofillSession().getRoot().getFocused()
            || !session.getAutofillSession().getRoot().getDomain().equals(Util.DOMAIN_AOD)
        ) {
            return null;
        }

        List<Autocomplete.LoginEntry> validEntries = new ArrayList<>();
        for(LoginSelectOption opt : request.options) {
            if(opt.value.formActionOrigin != null || opt.value.httpRealm != null)
                validEntries.add(opt.value);
        }
        if(validEntries.isEmpty()) {
            Log.w(TAG, "onLoginSelect: No valid entries!");
            return null;
        }

        //Currently only one account can be saved in-app
        GeckoResult<PromptResponse> res =  new GeckoResult<>();
        res.complete(request.confirm(
            new Autocomplete.LoginSelectOption(validEntries.get(0))
        ));

        return res;
    }

}
