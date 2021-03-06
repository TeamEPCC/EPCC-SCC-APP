package com.uoe.epcc.scc.epccscccluster

import android.animation.ArgbEvaluator
import android.content.Context
import android.content.res.Configuration
import android.graphics.Point
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import com.bumptech.glide.Glide
import com.github.clans.fab.FloatingActionButton
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.detail_view.*
import kotlinx.android.synthetic.main.info_view.view.*
import java.util.*
import android.net.Uri
import android.support.v4.content.res.ResourcesCompat
import android.animation.ValueAnimator


class MainActivity : AppCompatActivity() {

    var actState: ActState = ActState.INIT

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Activity state
        actState = ActState.INIT

        // Load locale
        loadLocale()

        // Load layout
        setContentView(R.layout.activity_main)

        // Load Video
        loadBgVideo()

        // Load Lang FAV
        loadFAV()

        // Setup share FAV
        setUpShareFAV()

        // Load server image
        Glide.with(this).load(R.drawable.teamepcc_logo_alt).into(img_cluster)

        // Enter immersive mode
        hideSystemUI()

        // Register onClick for Menu Buttons
        opt_team.setOnClickListener { showDetail(Option.TEAM) }
        opt_cpu.setOnClickListener { showDetail(Option.CPU) }
        opt_gpu.setOnClickListener { showDetail(Option.GPU) }
        opt_network.setOnClickListener { showDetail(Option.NETWORK) }
        opt_chassis.setOnClickListener { showDetail(Option.CHASSIS) }
        opt_software.setOnClickListener { showDetail(Option.SOFTWARE) }
        opt_storage.setOnClickListener { showDetail(Option.STORAGE) }
        opt_memory.setOnClickListener { showDetail(Option.MEMORY) }

        // Set onClick for Detail view
        frg_detail_view.setOnClickListener { hideDetail() }

        // Set onClick for Lang buttons
        fab_item_1.setOnClickListener { updateFAB(it as FloatingActionButton) }
        fab_item_2.setOnClickListener { updateFAB(it as FloatingActionButton) }
        fab_item_3.setOnClickListener { updateFAB(it as FloatingActionButton) }

    }

    override fun onBackPressed() {
        if (actState == ActState.INIT){
            super.onBackPressed()
        } else if (actState == ActState.INFO) {
            hideDetail()
        } else if (actState == ActState.SOCIAL) {
            hideShare()
        }
    }

    override fun onResume() {
        super.onResume()
        // Load locale
        loadLocale()
        // Enter immersive mode
        hideSystemUI()

        //Load video
        loadBgVideo()
    }

    override fun onPause() {
        super.onPause()

        // Set videocover to R.color.bg so when resumed there's
        // no black bg on VideoView
        video_cover.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.bg, null))
    }

    /**
     * Enter Immersive mode. Hides status and navigation bars, both
     * will be still reachable by swiping up/down on the edge of the
     * screen
     * */
    private fun hideSystemUI() {
        window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                or View.SYSTEM_UI_FLAG_FULLSCREEN
                or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
    }

    /**
     * Load background animation video
     * */
    private fun loadBgVideo() {
        val video_path = "android.resource://" + packageName + "/" + R.raw.bg_blur
        val video_uri = Uri.parse(video_path)
        bg_video_view.setVideoURI(video_uri)

        // Set cover color before setting up videoview
        video_cover.setBackgroundColor(ResourcesCompat.getColor(resources, R.color.bg, null))
        // When ready, animate cover color to transparent
        bg_video_view.setOnPreparedListener { mp ->
            mp.isLooping = true
            // Animate color transition
            val colorFrom = ResourcesCompat.getColor(resources, R.color.bg, null)
            val colorTo = ResourcesCompat.getColor(resources, android.R.color.transparent, null)
            val colorAnimation = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo)
            colorAnimation.duration = 2500 // milliseconds
            colorAnimation.addUpdateListener { animator -> video_cover.setBackgroundColor(animator.animatedValue as Int) }
            colorAnimation.start()
        }
        bg_video_view.start()
    }

    /**
     * Load correct language
     * */
    private fun loadLocale() {
        // Get language from SharedPreferences
        val languagepref = getSharedPreferences("language", Context.MODE_PRIVATE)

        // Check if language preferences have been initialized
        if (languagepref.getString("languageToLoad", null) == null)
            initializeLangPrefs()

        // Get language preferences
        val lang = languagepref.getString("languageToLoad", null)

        // Set locale
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = Configuration()
        config.locale = locale
        baseContext.resources.updateConfiguration(config,
                baseContext.resources.displayMetrics)
    }

    /**
     * Load Lang FAV images correctly
     * */
    private fun loadFAV() {
        // Get language from SharedPreferences
        val languagepref = getSharedPreferences("language", Context.MODE_PRIVATE)

        // Get language preferences
        val lang = languagepref.getString("languageToLoad", null)
        val l1 = languagepref.getString("languageToLoad_1", null)
        val l2 = languagepref.getString("languageToLoad_2", null)
        val l3 = languagepref.getString("languageToLoad_3", null)

        // Update FAB (assign img src and tag correctly)
        fab_menu.menuIconView.setImageResource(localeToResource(lang))
        fab_menu.tag = lang
        fab_item_1.setImageResource(localeToResource(l1))
        fab_item_1.tag = l1
        fab_item_2.setImageResource(localeToResource(l2))
        fab_item_2.tag = l2
        fab_item_3.setImageResource(localeToResource(l3))
        fab_item_3.tag = l3
    }

    /**
     * Initialize language-related SharedPreferences
     * */
    private fun initializeLangPrefs() {
        val languagepref = getSharedPreferences("language", Context.MODE_PRIVATE)
        // Save to SharedPreferences
        val editor = languagepref.edit()
        editor.putString("languageToLoad", Language.EN.lang)
        editor.putString("languageToLoad_1", Language.ES.lang)
        editor.putString("languageToLoad_2", Language.EL.lang)
        editor.putString("languageToLoad_3", Language.IN.lang)
        editor.commit()
    }

    /**
     * Set up share FAV
     * */
    private fun setUpShareFAV() {
        fab_share_facebook.setOnClickListener { showShare(SocialN.FACEBOOK) }
        fab_share_instagram.setOnClickListener { showShare(SocialN.INSTAGRAM) }
        fab_share_twitter.setOnClickListener { showShare(SocialN.TWITTER) }
        fab_share_favorite.setOnClickListener {showShare(SocialN.FAVORITE)}

        share_qr.setOnClickListener { hideShare() }
    }

    /**
     * Show Share QR
     * */
    private fun showShare(socialn: SocialN) {
        // Set state
        actState = ActState.SOCIAL

        // Hide options
        hideAllOptions()

        // Load right QR
        val social_qr = when (socialn) {
            SocialN.TWITTER -> R.drawable.qr_twitter
            SocialN.INSTAGRAM -> R.drawable.qr_instagram
            SocialN.FACEBOOK -> R.drawable.qr_facebook
            SocialN.FAVORITE -> R.drawable.qr_teamfavorite
        }

        // Make QR visible
        share_qr.setImageResource(social_qr)

        // Make transparent
        share_qr.alpha = 0F

        // Enable
        share_qr.visibility = View.VISIBLE

        // Animate alpha
        share_qr.animate().alpha(1F)
    }

    /**
     * Hide share QR
     * */
    private fun hideShare() {
        share_qr.animate().alpha(0F).withEndAction { share_qr.visibility = View.GONE }
        showAllOptions()
        // Set state
        actState = ActState.INIT
    }

    /**
     * Show detail for given Option
     * */
    private fun showDetail(opt: Option) {
        // Set state
        actState = ActState.INFO

        // Hide all buttons
        hideAllOptions()

        // Get screen size
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        Log.i("OnClick", "Screen size is: x = ${size.x} and y = ${size.y}")

        // Set visible but flat (width = 0dp)
        frg_info_view.visibility = View.VISIBLE
        frg_detail_view.visibility = View.VISIBLE


        // Give it 1/3 of screen width
        frg_info_view.layoutParams.width = size.x * 2 / 5 + size.x % 5
        frg_detail_view.layoutParams.width = size.x * 3 / 5
        frg_info_view.requestLayout()
        frg_detail_view.requestLayout()

        // Animate (Slide in)
        frg_info_view.translationX = (size.x * 2 / 5 + size.x % 5).toFloat()
        frg_info_view.animate().translationX(0F)

        frg_detail_view.translationX = -(size.x * 3 / 5).toFloat()
        frg_detail_view.animate().translationX(0F)


        // Load DETAIL Image
        when (opt) {
            Option.TEAM -> Glide.with(this).load(R.drawable.detail_team).into(detail_image)
            Option.CPU -> Glide.with(this).load(R.drawable.detail_cpu_edit).into(detail_image)
            Option.GPU -> Glide.with(this).load(R.drawable.detail_gpu).into(detail_image)
            Option.NETWORK -> Glide.with(this).load(R.drawable.detail_network).into(detail_image)
            Option.CHASSIS -> Glide.with(this).load(R.drawable.detail_chassis).into(detail_image)
            Option.SOFTWARE -> Glide.with(this).load(R.drawable.detail_software).into(detail_image)
            Option.STORAGE -> Glide.with(this).load(R.drawable.detail_storage).into(detail_image)
            Option.MEMORY -> Glide.with(this).load(R.drawable.detail_memory).into(detail_image)
        }


        // Load INFO Title
        frg_info_view.txt_info_title.text = when (opt) {
            Option.TEAM -> resources.getText(R.string.str_the_team)
            Option.CPU -> resources.getString(R.string.str_cpu)
            Option.GPU -> resources.getString(R.string.str_gpu)
            Option.NETWORK -> resources.getString(R.string.str_network)
            Option.CHASSIS -> resources.getString(R.string.str_chassis)
            Option.SOFTWARE -> resources.getString(R.string.str_software)
            Option.STORAGE -> resources.getString(R.string.str_storage)
            Option.MEMORY -> resources.getString(R.string.str_memory)
        }

        // Load INFO Description
        frg_info_view.txt_info_description.text = when (opt) {
            Option.TEAM -> resources.getText(R.string.info_team)
            Option.CPU -> resources.getText(R.string.info_cpu)
            Option.GPU -> resources.getText(R.string.info_gpu)
            Option.NETWORK -> resources.getText(R.string.info_network)
            Option.CHASSIS -> resources.getText(R.string.info_chassis)
            Option.SOFTWARE -> resources.getText(R.string.info_software)
            Option.STORAGE -> resources.getText(R.string.info_storage)
            Option.MEMORY -> resources.getText(R.string.info_memory)
        }
    }

    /**
     * Show detail for given Option
     * */
    private fun hideDetail() {
        // Get screen size
        val display = windowManager.defaultDisplay
        val size = Point()
        display.getRealSize(size)
        Log.i("OnClick", "Screen size is: x = ${size.x} and y = ${size.y}")

        // Animate slide out
        frg_info_view.animate().translationX((size.x * 2 / 5 + size.x % 5).toFloat()).withEndAction { frg_info_view.visibility = View.INVISIBLE }
        frg_detail_view.animate().translationX(-(size.x * 3 / 5).toFloat()).withEndAction { frg_detail_view.visibility = View.INVISIBLE }

        // Disable overlay
        showAllOptions()

        // Set state
        actState = ActState.INIT
    }

    /**
     * Hide all Options buttons
     * */
    private fun hideAllOptions() {
        // GONE options
        opt_team.animate().alpha(0F).withEndAction { opt_team.visibility = View.GONE }
        opt_cpu.animate().alpha(0F).withEndAction { opt_cpu.visibility = View.GONE }
        opt_gpu.animate().alpha(0F).withEndAction { opt_gpu.visibility = View.GONE }
        opt_network.animate().alpha(0F).withEndAction { opt_network.visibility = View.GONE }
        opt_chassis.animate().alpha(0F).withEndAction { opt_chassis.visibility = View.GONE }
        opt_software.animate().alpha(0F).withEndAction { opt_software.visibility = View.GONE }
        opt_storage.animate().alpha(0F).withEndAction { opt_storage.visibility = View.GONE }
        opt_memory.animate().alpha(0F).withEndAction { opt_memory.visibility = View.GONE }

        // INVISIBLE cluster image
        img_cluster.animate().alpha(0F).withEndAction { img_cluster.visibility = View.INVISIBLE }

        // GONE lang FAV
        fab_menu.animate().alpha(0F).withEndAction { fab_menu.visibility = View.GONE }
        fab_menu_share.animate().alpha(0F).withEndAction { fab_menu_share.visibility = View.GONE }

    }

    /**
     * Show all Options buttons
     * */
    private fun showAllOptions() {
        // Enable options button
        opt_team.visibility = View.VISIBLE
        opt_cpu.visibility = View.VISIBLE
        opt_gpu.visibility = View.VISIBLE
        opt_network.visibility = View.VISIBLE
        opt_chassis.visibility = View.VISIBLE
        opt_software.visibility = View.VISIBLE
        opt_storage.visibility = View.VISIBLE
        opt_memory.visibility = View.VISIBLE

        img_cluster.visibility = View.VISIBLE

        // Disable lang buttons
        fab_menu.visibility = View.VISIBLE
        fab_menu_share.visibility = View.VISIBLE

        opt_team.animate().alpha(1F)
        opt_cpu.animate().alpha(1F)
        opt_gpu.animate().alpha(1F)
        opt_network.animate().alpha(1F)
        opt_chassis.animate().alpha(1F)
        opt_software.animate().alpha(1F)
        opt_storage.animate().alpha(1F)
        opt_memory.animate().alpha(1F)

        img_cluster.animate().alpha(1F)

        fab_menu.animate().alpha(1F)
        fab_menu_share.animate().alpha(1F)


    }

    /**
     * Change app language
     * */
    private fun updateLang(lang: String, old: String) {
        // Load SharedPreferences
        val languagepref = getSharedPreferences("language", Context.MODE_PRIVATE)

        // Check if we really need to update language
        if (languagepref.getString("languageToLoad", "") != lang) {
            // Change locale
            val locale = Locale(lang)
            Locale.setDefault(locale)
            val config = Configuration()
            config.locale = locale
            baseContext.resources.updateConfiguration(config,
                    baseContext.resources.displayMetrics)

            // Save to SharedPreferences
            val editor = languagepref.edit()
            editor.putString("languageToLoad", lang)
            // Save the old lang
            for (pref in languagepref.all) {
                if (pref.value == lang) {
                    editor.putString(pref.key, old)
                }
            }
            // Commit changes
            editor.commit()

            // Realod activity
            finish()
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
            startActivity(intent)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
        }
    }

    /**
     * Update FAB options and language
     * */
    private fun updateFAB(view: FloatingActionButton) {
        // Save current locale
        val saveLocale = fab_menu.tag.toString()

        // Set fab_menu tag and resource
        fab_menu.tag = view.tag.toString()
        fab_menu.menuIconView.setImageResource(localeToResource(view.tag.toString()))

        // Set view tag and resource
        view.tag = saveLocale
        view.setImageResource(localeToResource(saveLocale))

        // Collapse FAB
        fab_menu.close(true)

        // Update language
        updateLang(fab_menu.tag.toString(), view.tag.toString())
    }

    /**
     * Return the resource that matches the received locale code
     * */
    private fun localeToResource(lang: String): Int {
        return when (lang) {
            Language.EN.lang -> R.mipmap.locale_en_56dp
            Language.ES.lang -> R.mipmap.locale_es_56dp
            Language.EL.lang -> R.mipmap.locale_el_56dp
            Language.IN.lang -> R.mipmap.locale_in_56dp
            else -> R.mipmap.locale_en_56dp
        }
    }


    enum class Option { TEAM, CPU, GPU, NETWORK, CHASSIS, SOFTWARE, STORAGE, MEMORY }
    enum class Language(val lang: String) { EN("en"), ES("es"), IN("in"), EL("el") }
    enum class SocialN { TWITTER, INSTAGRAM, FACEBOOK, FAVORITE }
    enum class ActState{INIT, SOCIAL, INFO}
}
