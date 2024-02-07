package com.fortinge.prompter.ui

import PrefRepository
import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Typeface
import android.net.Uri
import android.os.*
import android.provider.Settings
import android.text.method.ScrollingMovementMethod
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.fortinge.forprompt.R
import com.fortinge.forprompt.databinding.FragmentPromptBinding
import com.fortinge.prompter.utils.EventbusDataEvents
import com.fortinge.prompter.utils.OnSwipeTouchListener
import com.fortinge.prompter.viewmodel.SharedViewModel
import kotlinx.coroutines.delay
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.io.*
import java.lang.Runnable
import kotlin.math.max
import kotlin.math.min

import kotlin.math.roundToInt


class PromptFragment : Fragment() {

    private var _binding: FragmentPromptBinding? = null
    private val binding get() = _binding!!

    private lateinit var sharedViewModel: SharedViewModel

    private lateinit var sharedPref: PrefRepository

    var runnable: Runnable = Runnable { }
    var handler: Handler = Handler(Looper.getMainLooper())

    var promptSpeed = 1
    var promptEnable = false
    var textHeight = 0
    var fontSize = 70f
    var promptPosition = 0
    var screenHeight = 0
    var screenWidth = 0
    var isFlipped = false
    var isMirrored = false
    var promptSpeedRate = 1.0
    var countdownEnable = false
    var countdownValue = 0
    var isLooping = false
    var speedBarValue = 0f
    var promptBarInvisible = false
    var showScroll = false
    var showCue=false
    var cueMarkerSeekBarPosition=5


    var sliderWorkStartPrompt = false
    lateinit var activeFragment: Fragment
    var scrollRate = 100
    var scrollingTextMovementArea = 1
    var scrollingHandlerMovementArea = 1
    val MIN_FONT_SIZE = 10f
    val MAX_FONT_SIZE = 190f

    lateinit var scaleGestureDetector: ScaleGestureDetector

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        println("create")


       // okGoster()
        retainInstance = true

        if (!EventBus.getDefault().isRegistered(this)){
            EventBus.getDefault().register(this)
        }

        _binding = FragmentPromptBinding.inflate(inflater, container, false)
        val view = binding.root

        sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
        sharedPref = PrefRepository(requireContext())



        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            activity?.window?.insetsController?.hide(WindowInsets.Type.statusBars() or WindowInsets.Type.navigationBars())
        } else {
            activity?.window?.decorView?.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        }

        screenHeight = Resources.getSystem().displayMetrics.heightPixels
        screenWidth = Resources.getSystem().displayMetrics.widthPixels

        return view
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //sharedViewModel = ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)

        setVariablesForStart()

        setButtonsClick()

        getSharedPref()

        textViewSwipe()
        //okGoster()

        binding.scrollingTextView.setText(sharedPref.getText())
        calcTopBottom()

        sharedViewModel.promptPosition.observe(viewLifecycleOwner, {
            binding.scrollingTextView.scrollY = it
            //binding.gosterButon.isGone=true
        })

        sharedViewModel.promptEnable.observe(viewLifecycleOwner, {
            if (it) {
                startPrompt()
                //okGoster()
            }
        })


        sharedViewModel.isBold.observe(viewLifecycleOwner, Observer { isBold ->
            isBold?.let {
                if (it) {
                    println(it)
                    binding.scrollingTextView.setTypeface(null, Typeface.BOLD)
                } else {
                    println(it)
                    binding.scrollingTextView.setTypeface(null, Typeface.NORMAL)
                }
            }
        })

        sharedViewModel.promptingLoop.observe(viewLifecycleOwner, {
            isLooping = it
        })

        sharedViewModel.showScroll.observe(viewLifecycleOwner, Observer {
            showScroll = it
        })




        sharedViewModel.showCue.observe(viewLifecycleOwner,Observer {
            showCue = it

        })

        sharedViewModel.cueMarkerSeekBarPosition.observe(viewLifecycleOwner,Observer {
            cueMarkerSeekBarPosition = it

        })





        sharedViewModel.alignment.observe(viewLifecycleOwner, Observer {
            binding.scrollingTextView.textAlignment = it
            calcTopBottom()
        })

        sharedViewModel.fontSize.observe(viewLifecycleOwner, Observer {
            binding.scrollingTextView.textSize = it
            calcTopBottom()
        })

        sharedViewModel.text.observe(viewLifecycleOwner, Observer {
            println("text observe ediliyor mu??")
            if (it.isNullOrEmpty()) {
                binding.scrollingTextView.setText(R.string.prompt_default_text)
                calcTopBottom()
                println("text boş")
            } else {
                println("text boş değil")
                binding.scrollingTextView.setText(it)
                calcTopBottom()
            }

        })

        sharedViewModel.speedRate.observe(viewLifecycleOwner, Observer {
            it.let {
                promptSpeedRate = it
            }
        })

        sharedViewModel.countdownEnable.observe(viewLifecycleOwner, Observer {
                countdownEnable = it

        })

        sharedViewModel.countdownValue.observe(viewLifecycleOwner, {
            countdownValue = it
        })
        sharedViewModel.marginsEnable.observe(viewLifecycleOwner, Observer {
            it.let {
                if (it) {
                    if (sharedViewModel.marginsValue != null) {
                        val marginRate = (screenWidth * sharedViewModel.marginsValue.value!! / 200)
                        println("marginrate:" + marginRate)
                        binding.scrollingTextView.setPadding(marginRate, 0, marginRate, 0)
                        calcTopBottom()
                    }
                }
            }
        })

        sharedViewModel.backgroundColor.observe(viewLifecycleOwner, Observer {
            binding.scrollingTextView.setBackgroundColor(it)
            println("background observed")

        })

        sharedViewModel.textColor.observe(viewLifecycleOwner, {
            binding.scrollingTextView.setTextColor(it)
        })

        sharedViewModel.activeFragment.observe(viewLifecycleOwner, {
            activeFragment = it
            if (activeFragment != requireActivity().supportFragmentManager.fragments.get(3)) {
                stopPrompt()
            }
        })

    }

    private fun getSharedPref() {
        binding.scrollingTextView.setText(sharedPref.getText())

        binding.scrollingTextView.setBackgroundColor(sharedPref.getBackgroundColor())
//        binding.scrollingTextView.setTextColor(sharedPref.getTextColor())
        //okGoster()
        val isBold = sharedPref.getBold()
        if (isBold) {
            binding.scrollingTextView.setTypeface(null, Typeface.BOLD)
        } else {
            binding.scrollingTextView.setTypeface(null, Typeface.NORMAL)
        }

        binding.scrollingTextView.textAlignment = sharedPref.getAlignment()

        binding.scrollingTextView.textSize = sharedPref.getFontSize()

        sliderWorkStartPrompt = false
        speedBarValue = sharedPref.getPromptSpeed()
        binding.promptSpeedBar.value = speedBarValue


        promptSpeedRate = sharedPref.getSpeedRate()

        countdownEnable = sharedPref.getCountdownEnable()

        countdownValue = sharedPref.getCountdownValue()

        isLooping = sharedPref.getPromptLoop()

        showScroll = sharedPref.getShowScroll()
       // showCue=     sharedPref.getShow()

        calcTopBottom()
    }

    fun calcTopBottom() {
        textHeight =
            binding.scrollingTextView.lineCount * binding.scrollingTextView.lineHeight
        scrollingTextMovementArea = textHeight - binding.scrollingTextView.height + binding.promptBarLay.height
//        if (scrollingTextMovementArea <= 0){
//            scrollingHandlerMovementArea = binding.scrollingTextView.height
//        }
        scrollingHandlerMovementArea = binding.promptScrollBar.height - binding.promptScrollHandler.height - 30

        scrollRate = scrollingTextMovementArea / scrollingHandlerMovementArea
        if (scrollRate == 0){
            scrollRate = 1
        }
        println("scrollrate:::::: $scrollRate")
        println("movement:::::: $scrollingTextMovementArea")
        println("handler:::::: $scrollingHandlerMovementArea")
    }


    fun setButtonsClick() {
        //Start-Stop
        binding.startStopBtn.setOnClickListener {
            if (promptEnable) {
                stopPrompt()
            } else {
                if (binding.startStopText.text == getString(R.string.start) && countdownEnable) {
                    val intent = Intent(requireContext(), CountdownActivity::class.java)
                    intent.putExtra("timer", sharedViewModel.countdownValue.value)
                    startActivityForResult(intent, COUNTDOWN_ACTIVITY)

                } else {
                    startPrompt()
                    if (promptSpeed == 0) {
                        promptSpeed = 1
                        speedBarValue = promptSpeed.toFloat()
                        binding.promptSpeedBar.value = speedBarValue
                    }
                }
                Handler(Looper.getMainLooper()).post { binding.startStopText.setText(getString(R.string.stop)) }
            }

        }
//okGoster()
        //Top-Bottom
        binding.topBtn.setOnClickListener {
            if (promptEnable) {
                stopPrompt()
            }
            binding.scrollingTextView.scrollTo(0, 0)
            binding.promptScrollHandler.y = 0f
            binding.startStopBtn.background = context?.getDrawable(R.drawable.start_new)


            Handler(Looper.getMainLooper()).post { binding.startStopText.setText(getString(R.string.start)) }

        }

        binding.bottomBtn.setOnClickListener {
            if (promptEnable) {
                stopPrompt()
            }

            textHeight =
                binding.scrollingTextView.lineHeight * binding.scrollingTextView.lineCount
            if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ){
                if (promptBarInvisible) {
                    binding.scrollingTextView.scrollTo(0, textHeight - binding.scrollingTextView.height )
                } else {
                    binding.scrollingTextView.scrollTo(0, textHeight - binding.scrollingTextView.height + binding.promptBarLay.height)
                }
            } else {
                if (promptBarInvisible) {
                    binding.scrollingTextView.scrollTo(0, textHeight - binding.scrollingTextView.height)
                } else {
                    binding.scrollingTextView.scrollTo(0, textHeight - binding.scrollingTextView.height + binding.promptBarLay.height )
                }
            }


            binding.promptScrollHandler.y = (binding.promptScrollBar.height - binding.promptScrollHandler.height).toFloat()
//            println("bottom bas:::::: ${textHeight}")
//            println("prompt position: ${binding.scrollingTextView.scrollY}")
//            println("label height:::: ${binding.scrollingTextView.height}")
//            println("satır yükkkkk::: ${binding.scrollingTextView.lineHeight}")
//            println("satır sayısııı:: ${binding.scrollingTextView.lineCount}")

        }
        binding.gosterButon.isGone=true
        //binding.cueMarkerArrow.isGone=true


        //Flip-Mirror
        binding.flipBtn.setOnClickListener {
            if (isFlipped) {
                isFlipped = false
                binding.scrollingTextView.scaleX = 1f
                //binding.frameLayout.scaleX = 1f
               // binding.promptBarLay.scaleX=1f

                println("düz")
            } else {
                isFlipped = true
                binding.scrollingTextView.scaleX = -1f
                //binding.promptBarLay.scaleX=-1f
                //binding.frameLayout.scaleX = -1f

                println("ters")
            }

        }

        binding.mirrorBtn.setOnClickListener {
            if (isMirrored) {
                isMirrored = false
                binding.scrollingTextView.scaleY = 1f


                println("aşağı")
            }

            else {
                isMirrored = true
                binding.scrollingTextView.scaleY = -1f


                println("yukarı")
            }

//            binding.scrollingTextView.scrollTo(0, binding.scrollingTextView.scrollY + 500)
//            println("prompt position: ${binding.scrollingTextView.scrollY}")
        }


        binding.openFileBtn.setOnClickListener {

            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(
                    arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE),
                    STORAGE_PERMISSION_CODE
                )
            } else {
                val intent = Intent()
                    .setType("text/*")
                    .setAction(Intent.ACTION_GET_CONTENT)
                startActivityForResult(
                    Intent.createChooser(intent, getString(R.string.selectafile)),
                    STORAGE_INTENT_CODE
                )
            }

        }

        binding.promptSpeedBar.addOnChangeListener { slider, value, fromUser ->
            promptSpeed = value.toInt()
            speedBarValue = value

            if (promptSpeed == 0) {
                stopPrompt()
            } else if (!promptEnable && promptSpeed != 0) {
                startPrompt()
            }

        }






        binding.promptMinusBtn.setOnClickListener {
            if (speedBarValue != binding.promptSpeedBar.valueFrom) {
                speedBarValue--
                if (speedBarValue < binding.promptSpeedBar.valueFrom)
                    speedBarValue = binding.promptSpeedBar.valueFrom

                promptSpeed = speedBarValue.toInt()
                binding.promptSpeedBar.value = speedBarValue
            }

        }

        binding.promptPlusBtn.setOnClickListener {
            if (speedBarValue != binding.promptSpeedBar.valueTo) {
                speedBarValue++
                if (speedBarValue > binding.promptSpeedBar.valueTo)
                    speedBarValue = binding.promptSpeedBar.valueTo

                promptSpeed = speedBarValue.toInt()
                binding.promptSpeedBar.value = speedBarValue
            }

        }


    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_CODE && grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            val intent = Intent()
                .setType("text/*")
                .setAction(Intent.ACTION_GET_CONTENT)
            startActivityForResult(
                Intent.createChooser(intent, getString(R.string.selectafile)),
                STORAGE_INTENT_CODE
            )
        } else if (requestCode == STORAGE_PERMISSION_CODE && !shouldShowRequestPermissionRationale(
                permissions[0]
            )
        ) {
            val alert = AlertDialog.Builder(requireContext())
            alert.setTitle("Permission denied")
            alert.setMessage("Go to Settings and grant the permission to use this feature.")
            alert.setPositiveButton(
                getString(R.string.opensettings),
                { dialogInterface: DialogInterface, i: Int ->
                    val intent = Intent()
                        .setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    val uri = Uri.fromParts("package", activity?.packageName, null)
                    intent.setData(uri)
                    activity?.startActivity(intent)
                })
            alert.setNegativeButton(getString(R.string.cancel), { dialogInterface: DialogInterface, i: Int ->

            })
            alert.show()
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == STORAGE_INTENT_CODE && resultCode == Activity.RESULT_OK) {
            val selectedFile = data?.data
            if (data != null) {
                val inputStream =
                    requireActivity().contentResolver.openInputStream(selectedFile!!)

                val outputStream = ByteArrayOutputStream()
                inputStream.use { input ->
                    outputStream.use { output ->
                        input?.copyTo(output)
                    }
                }
                val byteArray = outputStream.toByteArray()
                val outputString = String(byteArray, Charsets.UTF_8)
                println(outputString)
                binding.scrollingTextView.setText(outputString)
                sharedViewModel.setText(outputString)

                calcTopBottom()
//                sharedPref.setText(outputString)
            }
        }

        if (requestCode == COUNTDOWN_ACTIVITY) {
            startPrompt()
        }
    }

    fun setVariablesForStart() {
        binding.scrollingTextView.setText(getString(R.string.prompt_default_text))
        binding.scrollingTextView.isVerticalScrollBarEnabled = true
        binding.scrollingTextView.movementMethod = ScrollingMovementMethod()
        promptSpeed = binding.promptSpeedBar.value.toInt()

        if (sharedViewModel.isBold.value == true) {
            binding.scrollingTextView.setTypeface(null, Typeface.BOLD)
        } else {
            binding.scrollingTextView.setTypeface(null, Typeface.NORMAL)
        }

        activeFragment = requireActivity().supportFragmentManager.fragments.get(3)
        calcTopBottom()
    }



    @SuppressLint("ClickableViewAccessibility")
    fun textViewSwipe() {

        binding.promptScrollHandler.setOnTouchListener (object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {

                if (event != null) {
                    val x = event.rawX
                    val y = event.rawY
                    println("eventy: " + y + "|| eventx: " + x + "|| height: " +binding.promptScrollBar.height)
                    if (event.action == MotionEvent.ACTION_MOVE) {
                        binding.promptScrollHandler.y = y
                        val scrollHandlerHeight = binding.promptScrollHandler.height
                        if (binding.promptScrollHandler.y < 10f) {
                            binding.promptScrollHandler.y = 10f
                        } else if (binding.promptScrollHandler.y > (binding.promptScrollBar.height - scrollHandlerHeight).toFloat()) {
                            binding.promptScrollHandler.y = (binding.promptScrollBar.height - scrollHandlerHeight).toFloat()
                        }

                        binding.scrollingTextView.scrollTo(0,
                            ((binding.promptScrollHandler.y - 10) * scrollRate).toInt()
                        )
                        println("handlerY:  ${binding.promptScrollHandler.y} ||||| textY: ${binding.scrollingTextView.scrollY}")

                    }

                }

                return true
            }
        })

        binding.scrollingTextView.setOnTouchListener(object :
            OnSwipeTouchListener(requireContext()) {
            override fun onSwipeUp(diffY: Float) {
                super.onSwipeUp(diffY)
                println(diffY)
                promptSpeed = -(diffY / 90).toInt()
                speedBarValue = promptSpeed.toFloat()
                if (speedBarValue > binding.promptSpeedBar.valueTo) speedBarValue = binding.promptSpeedBar.valueTo
                if (speedBarValue < binding.promptSpeedBar.valueFrom) speedBarValue = binding.promptSpeedBar.valueFrom
                binding.promptSpeedBar.value = speedBarValue
                startPrompt()
            }

            override fun onSwipeDown(diffY: Float) {
                super.onSwipeDown(diffY)
                println(diffY)
                promptSpeed = -(diffY / 90).toInt()
                speedBarValue = promptSpeed.toFloat()
                if (speedBarValue > binding.promptSpeedBar.valueTo) speedBarValue = binding.promptSpeedBar.valueTo
                if (speedBarValue < binding.promptSpeedBar.valueFrom) speedBarValue = binding.promptSpeedBar.valueFrom
                binding.promptSpeedBar.value = speedBarValue
                startPrompt()
            }

            override fun onScaleBegin() {

                super.onScaleBegin()
            }

            override fun onScale() {
                stopPrompt()
                if (scaleFactor - beginScaleFactor > 0.1){
                    //fontSize +=5
                    fontSize +=1

                    //fontSize = (fontSize * 1.033).toFloat()
                } else if (scaleFactor - beginScaleFactor < -0.1){
                    //fontSize -=5
                    fontSize -=1

                   // fontSize = (fontSize / 1.033).toFloat()

                }

                fontSize = min(MAX_FONT_SIZE, max(fontSize, MIN_FONT_SIZE))
                binding.scrollingTextView.textSize = fontSize
                super.onScale()

            }

            override fun onScaleEnd() {
                println("--------------scale end-------")
                println("")
                sharedViewModel.setFontSize(fontSize)
                sharedPref.setFontSize(fontSize)
                calcTopBottom()
                super.onScaleEnd()
            }
///EkranaTekDokunus
            override fun onSingleTapConfirmed() {
                super.onSingleTapConfirmed()
                println("single")

                if (promptEnable||promptBarInvisible) {
                    stopPrompt()
                    //okGoster()


    binding.gosterButon.isGone=false
                    //doControlPromptBarInvisible(true)
                    binding.gosterButon.setOnClickListener {
                        //binding.promptBarLay.isGone = false
                        doControlPromptBarInvisible(false)
                        binding.gosterButon.isGone=true
                        binding.gizleButon.isGone=false
                    }

                } else {

                    doControlPromptBarInvisible(true, false)

                    if (speedBarValue == 0f){
                        speedBarValue = 1f
                        binding.promptSpeedBar.value = speedBarValue
                    }
                    startPrompt()
                }
            }

            //override fun onDoubleClick() {
                //super.onDoubleClick()
                //println("doouble")
                //if (promptBarInvisible) {
                    //binding.gosterButon.isGone=false
                    //doControlPromptBarInvisible(false)
                    //} else {
                    //doControlPromptBarInvisible(true, false)
                    //  }

                //}

        })

    }


    fun startPrompt() {
        binding.gizleButon.isGone=true
        binding.gosterButon.isGone=true
        //cueMarkerShow()
       // cueMarkerPosition()
        if (promptEnable) {
            return
        }
        calcTopBottom()
        println("start promptta kontrol çalışmakta mıdır?")
        speedBarValue = promptSpeed.toFloat()
        speedBarValue = max(binding.promptSpeedBar.valueFrom, min(speedBarValue, binding.promptSpeedBar.valueTo))
        binding.promptSpeedBar.value = speedBarValue

        runnable = object : Runnable {
            override fun run() {

                activity?.let {
                    promptPosition = binding.scrollingTextView.scrollY

                    promptPosition = promptPosition + promptSpeed //(promptSpeed * promptSpeedRate)

                    //y = (y + hiz).toInt()
                    //textView.scrollTo(0, y)
                    //scrollView.scrollTo(0,y)
                    //scrollView.isSmoothScrollingEnabled = true

                    textHeight =
                        binding.scrollingTextView.lineCount * binding.scrollingTextView.lineHeight
                    //scrollView.smoothScrollTo(0,promptPosition)
                    binding.scrollingTextView.scrollTo(0, promptPosition)
                    binding.promptScrollHandler.y = (binding.scrollingTextView.scrollY / scrollRate).toFloat()

                    handler.postDelayed(
                        this,
                        (8 / promptSpeedRate).toLong()
                    )

//                    println("promptPosition: ${promptPosition},  textheight: ${textHeight}")

//                val limitHighVal =
//                    max((textHeight), (binding.scrollingTextView.height - textHeight / 2))
//                val limitLowVal = min(
//                    (textHeight - binding.scrollingTextView.height / 2),
//                    binding.scrollingTextView.height
//                )
                    if ( Build.VERSION.SDK_INT < Build.VERSION_CODES.Q ){
                        if (promptPosition > textHeight) {
                            if (isLooping) {
                                binding.scrollingTextView.scrollY = -binding.scrollingTextView.height
                            } else {
                                binding.scrollingTextView.scrollY = 0
                                stopPrompt()
                            }
                        }
                        if (promptPosition < -1 * binding.scrollingTextView.height) {
                            if (isLooping) {
                                binding.scrollingTextView.scrollY = textHeight
                            } else {
                                binding.scrollingTextView.scrollY = 0
                                stopPrompt()
                            }
                        }
                    } else {
                        if (promptPosition > textHeight + (textHeight* 6 / 100)) {
                            if (isLooping) {
                                binding.scrollingTextView.scrollY = -binding.scrollingTextView.height
                            } else {
                                binding.scrollingTextView.scrollY = 0
                                stopPrompt()
                            }
                        }
                        if (promptPosition < -1 * binding.scrollingTextView.height - (textHeight* 6 / 100)) {
                            if (isLooping) {
                                binding.scrollingTextView.scrollY = textHeight + (textHeight* 6 / 100)
                            } else {
                                binding.scrollingTextView.scrollY = 0
                                stopPrompt()
                            }
                        }

                    }



                }
            }

        }
        promptEnable = true
        handler.post(runnable)
        binding.startStopBtn.background = context?.getDrawable(R.drawable.pause_new)
        Handler(Looper.getMainLooper()).post { binding.startStopText.setText(getString(R.string.stop)) }
        doControlPromptBarInvisible()

    }
    fun cueMarker()
    {
        if(showCue==true)
        {
            cueMarkerPositionVisible()
        }
        else
        {
            cueMarkerPositionInvisible()
        }

    }
    fun cueMarkerPositionInvisible()
    {
        binding.cueMarkerArrow0.isGone=true
        binding.cueMarkerArrow1.isGone=true
        binding.cueMarkerArrow2.isGone=true
        binding.cueMarkerArrow3.isGone=true
        binding.cueMarkerArrow4.isGone=true
        binding.cueMarkerArrow5.isGone=true
        binding.cueMarkerArrow6.isGone=true



    }
    fun cueMarkerPositionVisible()
    {
        if(cueMarkerSeekBarPosition==0)
        {
            binding.cueMarkerArrow0.isGone=false
            binding.cueMarkerArrow1.isGone=true
            binding.cueMarkerArrow2.isGone=true
            binding.cueMarkerArrow3.isGone=true
            binding.cueMarkerArrow4.isGone=true
            binding.cueMarkerArrow5.isGone=true
            binding.cueMarkerArrow6.isGone=true

        }
        else if(cueMarkerSeekBarPosition==1)
        {
            binding.cueMarkerArrow0.isGone=true
            binding.cueMarkerArrow1.isGone=false
            binding.cueMarkerArrow2.isGone=true
            binding.cueMarkerArrow3.isGone=true
            binding.cueMarkerArrow4.isGone=true
            binding.cueMarkerArrow5.isGone=true
            binding.cueMarkerArrow6.isGone=true
        }
        else if(cueMarkerSeekBarPosition==2)
        {

            binding.cueMarkerArrow0.isGone=true
            binding.cueMarkerArrow1.isGone=true
            binding.cueMarkerArrow2.isGone=false
            binding.cueMarkerArrow3.isGone=true
            binding.cueMarkerArrow4.isGone=true
            binding.cueMarkerArrow5.isGone=true
            binding.cueMarkerArrow6.isGone=true

        }
        else if(cueMarkerSeekBarPosition==3)
        {

            binding.cueMarkerArrow0.isGone=true
            binding.cueMarkerArrow1.isGone=true
            binding.cueMarkerArrow2.isGone=true
            binding.cueMarkerArrow3.isGone=false
            binding.cueMarkerArrow4.isGone=true
            binding.cueMarkerArrow5.isGone=true
            binding.cueMarkerArrow6.isGone=true
        }
        else if(cueMarkerSeekBarPosition==4)
        {

            binding.cueMarkerArrow0.isGone=true
            binding.cueMarkerArrow1.isGone=true
            binding.cueMarkerArrow2.isGone=true
            binding.cueMarkerArrow3.isGone=true
            binding.cueMarkerArrow4.isGone=false
            binding.cueMarkerArrow5.isGone=true
            binding.cueMarkerArrow6.isGone=true
        }
        else if(cueMarkerSeekBarPosition==5)
        {
            binding.cueMarkerArrow0.isGone=true
            binding.cueMarkerArrow1.isGone=true
            binding.cueMarkerArrow2.isGone=true
            binding.cueMarkerArrow3.isGone=true
            binding.cueMarkerArrow4.isGone=true
            binding.cueMarkerArrow5.isGone=false
            binding.cueMarkerArrow6.isGone=true
        }
        else if(cueMarkerSeekBarPosition==6)
        {
            binding.cueMarkerArrow0.isGone=true
            binding.cueMarkerArrow1.isGone=true
            binding.cueMarkerArrow2.isGone=true
            binding.cueMarkerArrow3.isGone=true
            binding.cueMarkerArrow4.isGone=true
            binding.cueMarkerArrow5.isGone=true
            binding.cueMarkerArrow6.isGone=false
        }

    }




    fun stopPrompt() {
        if (promptEnable)
            promptEnable = false;

        handler.removeCallbacksAndMessages(null)

        doControlPromptBarInvisible(true)

        binding.startStopBtn.background = context?.getDrawable(R.drawable.continue_new)
        Handler(Looper.getMainLooper()).post { binding.startStopText.setText(getString(R.string.continu)) }

    }



    fun doControlPromptBarInvisible(
        doInvisible: Boolean = true,
        doDelayedInvisible: Boolean = true



    ) {
        val navView = activity?.findViewById<View>(R.id.nav_view)
            //Buton ile prompt ekranındaki alt 2 bar gizleniyor(Asım)
        binding.gizleButon.setOnClickListener {
                binding.promptBarLay.isGone = true
           // doControlPromptBarInvisible(true)
               navView?.isGone=true
                binding.promptScrollBar.isGone = !showScroll
                binding.gizleButon.isGone=true
                binding.gosterButon.isGone=false
            //Buton ile prompt ekranındaki alt 2 bar gozukuyor(Asım)
        binding.gosterButon.setOnClickListener {
            //doControlPromptBarInvisible(true)
                binding.promptBarLay.isGone = false

               navView?.isGone=false
                binding.promptScrollBar.isGone = !showScroll
                binding.gosterButon.isGone=true
                binding.gizleButon.isGone=false
            }
        }

        if (doInvisible) {
            if (doDelayedInvisible) {
                Handler(Looper.getMainLooper()).postDelayed({
                    if (promptEnable && activeFragment == requireActivity().supportFragmentManager.fragments.get(
                            3
                        )
                    ) {
                        // 1 ms sonra alt bar kendiliginden gizlenir(Asım)
                        binding.promptBarLay.isGone = true
                        navView?.isGone = true//prompt,edit,settings barını saklar
                        binding.promptScrollBar.isGone = !showScroll
                        binding.gizleButon.isGone=true
                    }

                }, 1)
            }
            else
            {

                binding.promptBarLay.isGone = true
                navView?.isGone = true
                binding.gizleButon.isGone=true
               // binding.promptScrollBar.isGone = !showScroll

            }
            promptBarInvisible = true
        } else {
            binding.promptBarLay.isGone = false
            navView?.isGone = false
            // binding.promptScrollBar.isGone = false
            promptBarInvisible = false
            binding.gizleButon.isGone=false
            binding.gosterButon.isGone=true
        }

    }




    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        stopPrompt()
        calcTopBottom()
        cueMarker()
    }

    override fun onStart() {
        super.onStart()
        println("start")
    }

    override fun onResume() {
        super.onResume()
        cueMarker()
        println("resume")

    }

    override fun onStop() {
        super.onStop()
        cueMarker()
        println("stop")
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    internal fun bleDataReceivedEvent(bleData: EventbusDataEvents.BleDataChanged) {

        val promptFragment = requireActivity().supportFragmentManager.fragments.get(3)

        if (binding != null && activeFragment == promptFragment) {
            //cueMarkerPosition()
            when (bleData.data.toInt()) {
                1 -> {
                    if (!promptEnable){
                        if (speedBarValue <= 0f){
                            speedBarValue = 1f
                            binding.promptSpeedBar.value = speedBarValue
                            startPrompt()
                        } else {
                            startPrompt()
                        }

                    }
                    else
                        binding.promptPlusBtn.callOnClick()
                }

                2 -> {
                    if (!promptEnable){
                        if (speedBarValue >= 0f){
                            speedBarValue = -1f
                            binding.promptSpeedBar.value = speedBarValue
                            startPrompt()
                        } else {
                            startPrompt()
                        }
                    }
                    else
                        binding.promptMinusBtn.callOnClick()
                }

                4 -> if (promptEnable)
                    stopPrompt()
                else
                    binding.topBtn.callOnClick()

                8 -> if (promptEnable){
                    stopPrompt()
                }
                else {
                    binding.bottomBtn.callOnClick()

                }

            }


        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        println("attach")
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onDetach() {
        super.onDetach()
        println("detach")
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    override fun onPause() {
        super.onPause()
        println("pause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopPrompt()
        _binding = null
        println("destroy")
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
    }

    companion object {
        val STORAGE_PERMISSION_CODE = 1
        val STORAGE_INTENT_CODE = 2
        val COUNTDOWN_ACTIVITY = 3
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        sharedViewModel.setPromptPosition(promptPosition)
        sharedPref.setPromptSpeed(speedBarValue)
        sharedViewModel.setPromptEnable(promptEnable)

    }

}