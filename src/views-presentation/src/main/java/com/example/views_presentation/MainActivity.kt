package com.example.views_presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.views_presentation.presentation.C_ListViewAdapterActivity
import com.example.views_presentation.presentation.D_ListViewAdapterHolderActivity

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

//        startActivity<A_NaiveApproachActivity>()
//        startActivity<B_SimpleListViewActivity>()
//        startActivity<C_ListViewAdapterActivity>()
        startActivity<D_ListViewAdapterHolderActivity>()
    }

    private inline fun <reified A> startActivity() {
        val intent = Intent(this, A::class.java)
        startActivity(intent)
    }
}