package com.example.views_presentation

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.views_presentation.presentation.M_StylingActivity
import com.google.android.material.color.DynamicColors

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        DynamicColors.applyToActivitiesIfAvailable(this.application)

//        startActivity<A_NaiveApproachActivity>()
//        startActivity<B_SimpleListViewActivity>()
//        startActivity<C_ListViewAdapterActivity>()
//        startActivity<D_ListViewAdapterHolderActivity>()
//        startActivity<E_GridViewAdapterHolderActivity>()
//        startActivity<F_SimpleRecyclerViewActivity>()
//        startActivity<G_RecyclerViewNotificationActivity>()
//        startActivity<H_RecyclerViewDiffUtilActivity>()
//        startActivity<I_DataBindingActivity>()
//        startActivity<CounterActivity>()
//        startActivity<SignupActivity>()
        startActivity<M_StylingActivity>()
    }

    private inline fun <reified A> startActivity() {
        val intent = Intent(this, A::class.java)
        startActivity(intent)
    }
}