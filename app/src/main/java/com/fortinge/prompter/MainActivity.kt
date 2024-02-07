package com.fortinge.prompter

import android.os.Bundle
import android.os.PersistableBundle
import android.widget.Toast
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.get

import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.get
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.fortinge.forprompt.R
import com.fortinge.prompter.ui.EditFragment
import com.fortinge.prompter.ui.PromptFragment
import com.fortinge.prompter.ui.SettingsFragment
import com.fortinge.prompter.viewmodel.SharedViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var navController: NavController
    private lateinit var appBarConfiguration: AppBarConfiguration

    private lateinit var sharedViewModel: SharedViewModel
    lateinit var navView: BottomNavigationView

    private val promptFragment = PromptFragment()
    private val editFragment = EditFragment()

    private val settingsFragment = SettingsFragment()

    val fm = supportFragmentManager
    var active: Fragment = promptFragment


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        navView = findViewById(R.id.nav_view)

        sharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)


        fm.beginTransaction().add(R.id.nav_host_fragment, settingsFragment, "3").hide(settingsFragment).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, editFragment, "2").hide(editFragment).commit()
        fm.beginTransaction().add(R.id.nav_host_fragment, promptFragment, "1").commit()


        navView.setOnNavigationItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_prompt -> {
                    fm.beginTransaction().hide(active).show(promptFragment).commit()
                    active = promptFragment
                    sharedViewModel.setActiveFragment(active)
                    true
                }

                R.id.navigation_edit -> {
                    
//                    fm.beginTransaction().hide(active).show(editFragment).commit()
                    fm.beginTransaction().hide(promptFragment).hide(settingsFragment).show(editFragment).commit()
                    active = editFragment
                    sharedViewModel.setActiveFragment(active)
                    true

                }

                R.id.navigation_settings -> {
                    fm.beginTransaction().hide(active).show(settingsFragment).commit()
                    active = settingsFragment
                    sharedViewModel.setActiveFragment(active)
                    true
                }
                else -> false
            }


        }

    }

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount > 0 || !promptFragment.isHidden) {

            super.onBackPressed()
        } else {
            fm.beginTransaction().hide(active).show(promptFragment).commit()
            active = promptFragment
            sharedViewModel.setActiveFragment(active)
            navView.menu.findItem(R.id.navigation_prompt).isChecked = true

        }

    }


}