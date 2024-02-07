package com.fortinge.prompter.ui

import PrefRepository
import android.app.Activity
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.*
import android.view.View.OnTouchListener
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.marginLeft
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fortinge.forprompt.R
import com.fortinge.forprompt.databinding.FragmentEditBinding
import com.fortinge.prompter.utils.EventbusDataEvents
import com.fortinge.prompter.viewmodel.SharedViewModel
import com.google.android.material.tabs.TabLayout
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import java.io.*
import java.text.SimpleDateFormat
import java.util.*


class EditFragment : Fragment() {
    private var _binding: FragmentEditBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var sharedPref: PrefRepository

    private lateinit var clipboardManager: ClipboardManager

    private var editTextAlignment = 0
    private var editTextIsBold = false
    var showCue=false


    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?

    ): View? {

        _binding = FragmentEditBinding.inflate(inflater, container, false)
        val view = binding.root

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedPref = PrefRepository(requireContext())

        setVariablesForStart()
        setButtonClick()
        //okGoster()

        binding.editFontSizeBar.addOnChangeListener { slider, value, fromUser ->
            binding.editText.textSize = value
            sharedViewModel.setFontSize(value)
            sharedPref.setFontSize(value)

        }






        return view
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        val text = binding.editText.text.toString()
            sharedViewModel.setText(text)
            sharedPref.setText(text)
            //okGoster()

    }

    override fun onStop() {
        super.onStop()

        val text = binding.editText.text.toString()
            sharedViewModel.setText(text)
            sharedPref.setText(text)


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)




        getsharedPref()

        sharedViewModel.text.observe(viewLifecycleOwner, Observer {
            binding.editText.setText(it)
        })

        sharedViewModel.isBold.observe(viewLifecycleOwner, Observer {
            if (it) {
                binding.editText.setTypeface(null, Typeface.BOLD)
            } else {
                binding.editText.setTypeface(null, Typeface.NORMAL)
            }
        })

        sharedViewModel.alignment.observe(viewLifecycleOwner, Observer {
            editTextAlignment = it
            binding.editText.textAlignment = editTextAlignment
            when (editTextAlignment) {
                View.TEXT_ALIGNMENT_TEXT_START -> binding.alignmentText.setText(getString(R.string.left))
                View.TEXT_ALIGNMENT_CENTER     -> binding.alignmentText.setText(getString(R.string.center))
                View.TEXT_ALIGNMENT_TEXT_END   -> binding.alignmentText.setText(getString(R.string.right))
            }
        })

        sharedViewModel.fontSize.observe(viewLifecycleOwner, Observer{
            binding.editText.textSize = it
            binding.editFontSizeBar.value = it
        })




        sharedViewModel.backgroundColor.observe(viewLifecycleOwner, Observer {
            binding.editText.setBackgroundColor(it)
        })




        sharedViewModel.showCue.observe(viewLifecycleOwner,Observer {
            showCue = it

        })




        sharedViewModel.textColor.observe(viewLifecycleOwner, Observer {
            binding.editText.setTextColor(it)
        })
    }

    fun getsharedPref() {
        binding.editText.setText(sharedPref.getText())

        editTextIsBold = sharedPref.getBold()
        if (editTextIsBold){
            binding.editText.setTypeface(null, Typeface.BOLD)
        } else {
            binding.editText.setTypeface(null, Typeface.NORMAL)

        }


        editTextAlignment = sharedPref.getAlignment()
        binding.editText.textAlignment = editTextAlignment
        setTextAlign(true)

        binding.editFontSizeBar.value = sharedPref.getFontSize()
        binding.editText.textSize = binding.editFontSizeBar.value
        //binding.editCueMarkerPosition.value
//        binding.editText.setTextColor(sharedPref.getTextColor())
        binding.editText.setBackgroundColor(sharedPref.getBackgroundColor())
    }

    fun setVariablesForStart() {
        clipboardManager = context?.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        binding.editText.movementMethod = ScrollingMovementMethod()

        binding.editText.isEnabled = false
        binding.editText.isEnabled = true
    }

    fun setButtonClick() {
        binding.editText.setOnTouchListener(View.OnTouchListener { v, motionEvent ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                when (motionEvent.action and MotionEvent.ACTION_MASK) {
                    MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(
                        false
                    )
                }
            false
        })


        binding.boldBtn.setOnClickListener {
            if (editTextIsBold) {
                binding.boldBtn.setTypeface(null, Typeface.NORMAL)
                binding.editText.setTypeface(null, Typeface.NORMAL)
                editTextIsBold = false
            } else {
                binding.boldBtn.setTypeface(null, Typeface.BOLD)
                binding.editText.setTypeface(null, Typeface.BOLD)
                editTextIsBold = true
            }

            sharedViewModel.setIsBold(editTextIsBold)
            sharedPref.setBold(editTextIsBold)
        }

        binding.copyBtn.setOnClickListener {
            if (binding.editText.hasSelection()){
                val startSel = binding.editText.selectionStart
                val endSel = binding.editText.selectionEnd
                val textToCopy = binding.editText.text.subSequence(startSel, endSel)
                val clipData = ClipData.newPlainText("text", textToCopy)
                clipboardManager.setPrimaryClip(clipData)
            } else {
                val textToCopy = binding.editText.text
                val clipData = ClipData.newPlainText("text", textToCopy)
                clipboardManager.setPrimaryClip(clipData)
            }
                Toast.makeText(requireContext(), getString(R.string.textcopiedtoclipboard), Toast.LENGTH_LONG).show()

        }
       // binding.cueMarkerArrow.isGone=true

        binding.pasteBtn.setOnClickListener {
            val copiedText = clipboardManager.getPrimaryClip()

            if (copiedText != null) {
                val item = copiedText.getItemAt(0)
                val itemText = item!!.text.toString()
                val cursorPos = binding.editText.selectionStart
//                println(binding.editText.selectionStart)
                binding.editText.append(itemText, cursorPos, itemText.length+binding.editText.selectionStart)
//                binding.editText.append(itemText)
                Toast.makeText(requireContext(), getString(R.string.textpastedfromclipboard), Toast.LENGTH_SHORT).show()
            }

        }

        binding.clearBtn.setOnClickListener {
            //binding.editText.setText("")
            binding.editText.text.clear()
        }

        binding.alignmentBtn.setOnClickListener {
            setTextAlign()
        }

        binding.saveBtn.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
            } else {
                val calendar = Calendar.getInstance().time
                val simpleDateFormat = SimpleDateFormat("dd/MM/yy", Locale.getDefault())
                val formattedDate = simpleDateFormat.format(calendar)

                val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                    .addCategory(Intent.CATEGORY_OPENABLE)
                    .setType("text/plain")
                    .putExtra(Intent.EXTRA_TITLE, "FP_" + formattedDate)

                startActivityForResult(intent, STORAGE_INTENT_CODE)
            }

        }
    }

    private fun setTextAlign(firstLogin: Boolean =false) {
        if (!firstLogin)
        if (editTextAlignment == View.TEXT_ALIGNMENT_CENTER){
            editTextAlignment = View.TEXT_ALIGNMENT_TEXT_END
        } else if (editTextAlignment == View.TEXT_ALIGNMENT_TEXT_END) {
            editTextAlignment = View.TEXT_ALIGNMENT_TEXT_START
        } else if (editTextAlignment == View.TEXT_ALIGNMENT_TEXT_START) {
            editTextAlignment = View.TEXT_ALIGNMENT_CENTER
        }

        when (editTextAlignment){
            View.TEXT_ALIGNMENT_TEXT_END -> {
                binding.alignmentText.setText(getString(R.string.right))
                binding.alignmentBtn.background = (context?.getDrawable(R.drawable.right))
            }
            View.TEXT_ALIGNMENT_TEXT_START -> {
                binding.alignmentText.setText(getString(R.string.left))
                binding.alignmentBtn.background = (context?.getDrawable(R.drawable.left))
            }
            View.TEXT_ALIGNMENT_CENTER -> {
                binding.alignmentText.setText(getString(R.string.center))
                binding.alignmentBtn.background = (context?.getDrawable(R.drawable.center))
            }
        }

        binding.editText.textAlignment = editTextAlignment

        binding.editText.textSize = binding.editText.textSize +1f
        binding.editText.textSize = binding.editFontSizeBar.value
        println("textsize: "+binding.editText.textSize)

        sharedViewModel.setAlignment(editTextAlignment)
        sharedPref.setAlignment(editTextAlignment)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_INTENT_CODE && resultCode == Activity.RESULT_OK) {
            val selectedPath = data?.data

                if (selectedPath != null)
                    writeInFile(selectedPath, binding.editText.text.toString())
                else
                    Toast.makeText(requireContext(), getString(R.string.pathiswrong), Toast.LENGTH_LONG).show()



        }


    }


    private fun writeInFile(uri: Uri, text: String) {
        val outputStream: OutputStream
        try {
            outputStream = context?.contentResolver?.openOutputStream(uri)!!
            val bw = BufferedWriter(OutputStreamWriter(outputStream))
            bw.write(text)
            bw.flush()
            bw.close()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .setType("text/plain")

            startActivityForResult(intent, STORAGE_INTENT_CODE)

        } else if (requestCode == STORAGE_PERMISSION_CODE && !shouldShowRequestPermissionRationale(permissions[0])) {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Permission denied")
            alert.setMessage("Go to Settings and grant the permission to use this feature.")
            alert.setPositiveButton(getString(R.string.opensettings), { dialogInterface : DialogInterface, i: Int ->
                val intent = Intent()
                    .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", activity?.packageName,null)
                intent.setData(uri)
                activity?.startActivity(intent)
            })
            alert.setNegativeButton(getString(R.string.cancel), { dialogInterface : DialogInterface, i: Int ->

            })
            alert.show()

        }
    }
    @Subscribe
    internal fun bleDataReceivedEvent(bleData: EventbusDataEvents.BleDataChanged) {

    }



    override fun onAttach(context: Context) {
        super.onAttach(context)
        EventBus.getDefault().register(this)

    }

    override fun onDetach() {
        super.onDetach()
        EventBus.getDefault().unregister(this)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        println("destroy")
    }

    companion object {
        val STORAGE_PERMISSION_CODE = 1
        val STORAGE_INTENT_CODE = 2
    }


}