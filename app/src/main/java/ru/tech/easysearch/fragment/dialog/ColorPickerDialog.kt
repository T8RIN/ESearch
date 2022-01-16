package ru.tech.easysearch.fragment.dialog

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.transition.platform.MaterialFadeThrough
import ru.tech.easysearch.R
import ru.tech.easysearch.activity.BrowserActivity
import ru.tech.easysearch.activity.MainActivity
import ru.tech.easysearch.adapter.color.ColorPickerAdapter
import ru.tech.easysearch.data.DataArrays.colorList
import ru.tech.easysearch.data.SharedPreferencesAccess.GREEN
import ru.tech.easysearch.data.SharedPreferencesAccess.loadThemeVariant
import ru.tech.easysearch.data.SharedPreferencesAccess.saveTheme
import ru.tech.easysearch.helper.interfaces.SettingsInterface

class ColorPickerDialog : DialogFragment(), SettingsInterface {

    override fun onStart() {
        super.onStart()
        val dialog: Dialog? = dialog
        if (dialog != null) {
            val width = ViewGroup.LayoutParams.MATCH_PARENT
            val height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.window?.setLayout(width, height)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        currentColor = loadThemeVariant(context)!!
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val recycler: RecyclerView = view.findViewById(R.id.colorRecycler)
        recycler.adapter = ColorPickerAdapter(requireContext(), this, colorList, this)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialogBuilder =
            MaterialAlertDialogBuilder(requireActivity(), R.style.modeAlert)
                .setTitle(R.string.themeChooser)
                .setPositiveButton(R.string.ok_ok) { _, _ ->
                    dismiss()
                    if (currentColor != loadThemeVariant(requireContext())) {
                        saveTheme(requireContext(), currentColor)
                        when (val activity = requireActivity()) {
                            is BrowserActivity -> {
                                activity.startActivity(
                                    Intent(
                                        activity,
                                        MainActivity::class.java
                                    ).apply { flags = FLAG_ACTIVITY_CLEAR_TOP })
                                activity.finish()
                            }
                            else -> activity.recreate()
                        }
                    }
                }

        val view = requireActivity().layoutInflater.inflate(R.layout.theme_picker_dialog, null)
        onViewCreated(view, null)
        dialogBuilder.setView(view)
        return dialogBuilder.create()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enterTransition = MaterialFadeThrough().apply { duration = 200L }
        exitTransition = MaterialFadeThrough().apply { duration = 200L }
    }

    private var currentColor: String = GREEN

    override fun onPickColor(color: String) {
        currentColor = color
    }

}