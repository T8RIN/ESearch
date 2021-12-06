package ru.tech.easysearch.fragment.settings

import android.os.Bundle
import android.view.View
import androidx.fragment.app.DialogFragment
import ru.tech.easysearch.R

class SettingsFragment : DialogFragment(R.layout.settings_fragment) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.Theme_App_Dialog_FullScreen)
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        requireDialog().window?.setWindowAnimations(
            R.style.DialogAnimation
        )
    }

}