package com.sundaydavid.fastBite.ui.search

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ListAdapter
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.fevziomurtekin.customprogress.Dialog
import com.fevziomurtekin.customprogress.Type
import com.sundaydavid.fastBite.R
import com.sundaydavid.fastBite.adapter.AlphabetListAdapter
import com.sundaydavid.fastBite.adapter.SearchMealAdapter
import com.sundaydavid.fastBite.model.Meal
import com.sundaydavid.fastBite.model.SearchModel
import com.sundaydavid.fastBite.remoteDatabase.ApiClient
import com.sundaydavid.fastBite.utility.CellClickListener
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchFragment : Fragment() {

    private lateinit var searchViewModel: SearchBaseRepository
    private lateinit var recyclerView: RecyclerView
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var progress: Dialog
    val  dataList = ArrayList<Meal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        retainInstance = true
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        searchViewModel =
                ViewModelProvider(this).get(SearchBaseRepository::class.java)
        val root = inflater.inflate(R.layout.fragment_search, container, false)
        recyclerView = root.findViewById(R.id.meal_search_recyclerView)
        searchView = root.findViewById(R.id.meal_search)

        progress = root.findViewById(R.id.progress_bar)

        recyclerView.layoutManager = LinearLayoutManager(parentFragment?.context,
            LinearLayoutManager.VERTICAL, false)


        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
               return false

            }

            override fun onQueryTextChange(newText: String?): Boolean {

                if (isOnline(requireContext())){
                    progress.settype(Type.WEDGES)
                    progress.setdurationTime(100)
                    progress.show()
                    getMealList(newText!!)

                }else {
                    Toast.makeText(context, "Check network", Toast.LENGTH_SHORT).show()
                }
                return true
            }
        })

        return root
    }

    fun getMealList(meal: String) {

        ApiClient.getClient.SearchMeal(meal).enqueue(object : Callback<SearchModel> {
            override fun onResponse(call: Call<SearchModel>, response: Response<SearchModel>) {

                if (response.isSuccessful && response.body()?.meals != null){

                        dataList += response.body()!!.meals

                        recyclerView.adapter = SearchMealAdapter(activity!!.applicationContext, dataList)
                        recyclerView.adapter?.notifyDataSetChanged()
                        progress.isVisible = false

                        searchView.setQuery("", false)
//                    searchView.clearFocus()

                }

                else {
                    Toast.makeText(context, "Item not found", Toast.LENGTH_SHORT).show()
                    progress.isVisible = false
                }
            }

            override fun onFailure(call: Call<SearchModel>, t: Throwable) {
                t.message
                progress.isVisible = false
            }
        })
    }

    fun isOnline(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val capabilities =  connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            if (capabilities != null) {
                when {
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)-> {
                        return true
                    }
                    capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> {
                        return true
                    }
                }
            }
        } else {
            val activeNetworkInfo = connectivityManager.activeNetworkInfo
            if (activeNetworkInfo != null && activeNetworkInfo.isConnected){
                return true
            }
        }

        return false
    }


}
