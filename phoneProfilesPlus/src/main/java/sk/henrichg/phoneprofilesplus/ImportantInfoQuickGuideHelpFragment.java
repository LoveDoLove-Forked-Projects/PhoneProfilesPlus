package sk.henrichg.phoneprofilesplus;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

/** @noinspection ExtractMethodRecommender*/
public class ImportantInfoQuickGuideHelpFragment extends Fragment {

    //int scrollTo = 0;

    public ImportantInfoQuickGuideHelpFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_important_info_quick_guide, container, false);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final ImportantInfoActivity activity = (ImportantInfoActivity) getActivity();
        if (activity == null)
            return;

        TextView textView = view.findViewById(R.id.activity_info_quick_guide_sensors_texts);
        String text = StringConstants.TAG_LIST_START_FIRST_ITEM_HTML;
        text = text +                                            getString(R.string.important_info_quick_guide_sensors_2) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_3) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_4) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_5) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_6) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_7) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_8) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_sensors_9);
        text = text + StringConstants.TAG_LIST_END_LAST_ITEM_HTML;
        //noinspection DataFlowIssue
        textView.setText(StringFormatUtils.fromHtml(text, true,  false, 0, 0, false));

        textView = view.findViewById(R.id.activity_info_quick_guide_text_2);
        text = StringConstants.TAG_NUMBERED_LIST_START_FIRST_ITEM_HTML;
        text = text +                                            getString(R.string.important_info_quick_guide_2) + StringConstants.TAG_LIST_ITEM_END_HTML;
        text = text + StringConstants.TAG_LIST_ITEM_START_HTML + getString(R.string.important_info_quick_guide_3);
        text = text + StringConstants.TAG_NUMBERED_LIST_END_LAST_ITEM_HTML;
        //noinspection DataFlowIssue
        textView.setText(StringFormatUtils.fromHtml(text, false,  true, 1, 17, false));

        /*
        GlobalUtils.emailMe(view.findViewById(R.id.activity_info_notification_contact),
                getString(R.string.important_info_contact),
                "", getString(R.string.about_application_support_subject),
                GlobalUtils.getEmailBodyText(activity),
                activity);
        */
        final TextView supportText = view.findViewById(R.id.activity_info_notification_contact);
        if (supportText != null) {
            supportText.setText(getString(R.string.important_info_support) + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW);
            supportText.setOnClickListener(v -> {
                PopupMenu popup;
                popup = new PopupMenu(activity, supportText, Gravity.START | Gravity.BOTTOM);
                new MenuInflater(activity).inflate(R.menu.menu_support, popup.getMenu());

                Menu menu = popup.getMenu();
                MenuItem menuItem = menu.findItem(R.id.menu_discord);
                if (menuItem != null) {
                    SubMenu subMenu = menuItem.getSubMenu();
                    if (subMenu != null) {
                        Drawable triangle = ContextCompat.getDrawable(activity, R.drawable.ic_submenu_triangle);
                        if (triangle != null) {
                            triangle.setTint(ContextCompat.getColor(activity, R.color.activitySecondaryTextColor));
                            SpannableString headerTitle = new SpannableString("    " + menuItem.getTitle());
                            triangle.setBounds(0,
                                    GlobalGUIRoutines.sip(1),
                                    GlobalGUIRoutines.sip(10.5f),
                                    GlobalGUIRoutines.sip(8.5f));
                            headerTitle.setSpan(new ImageSpan(triangle, ImageSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            //headerTitle.setSpan(new ImageSpan(this, R.drawable.ic_submenu_triangle, DynamicDrawableSpan.ALIGN_BASELINE), 0, 1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            subMenu.setHeaderTitle(headerTitle);
                        }
                    }
                }

                popup.setOnMenuItemClickListener(activity::supportMenu);

                if (!activity.isFinishing())
                    popup.show();
            });
        }

        TextView translationTextView = view.findViewById(R.id.activity_info_translations);
        String str1 = getString(R.string.about_application_translations);
        String str2 = str1 + StringConstants.CHAR_NEW_LINE + PPApplication.CROWDIN_URL + StringConstants.STR_HARD_SPACE_DOUBLE_ARROW;
        Spannable spannable = new SpannableString(str2);
        //spannable.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), 0, str1.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void updateDrawState(TextPaint ds) {
                ds.setColor(ds.linkColor);    // you can use custom color
                ds.setUnderlineText(false);    // this remove the underline
            }

            @Override
            public void onClick(@NonNull View textView) {
                String url = PPApplication.CROWDIN_URL;
                Intent i = new Intent(Intent.ACTION_VIEW);
                i.setData(Uri.parse(url));
                try {
                    startActivity(Intent.createChooser(i, getString(R.string.web_browser_chooser)));
                } catch (Exception e) {
                    PPApplicationStatic.recordException(e);
                }
            }
        };
        spannable.setSpan(clickableSpan, str1.length()+1, str2.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        //sbt.setSpan(new UnderlineSpan(), str1.length()+1, str2.length(), 0);
        //noinspection DataFlowIssue
        translationTextView.setText(spannable);
        translationTextView.setMovementMethod(LinkMovementMethod.getInstance());
        /*AboutApplicationActivity.emailMe((TextView) view.findViewById(R.id.activity_info_translations),
                getString(R.string.important_info_translations),
                getString(R.string.about_application_translations2),
                getString(R.string.about_application_translations_subject),
                AboutApplicationActivity.getEmailBodyText(AboutApplicationActivity.EMAIL_BODY_TRANSLATIONS, activity),
                true, activity);*/
    }

}
