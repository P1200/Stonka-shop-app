package com.stonka.shopapp;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import static org.junit.Assert.assertTrue;

import android.os.Environment;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.espresso.assertion.ViewAssertions;
import androidx.test.espresso.matcher.ViewMatchers;
import androidx.test.rule.GrantPermissionRule;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
            new ActivityScenarioRule<>(MainActivity.class);

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(
                    android.Manifest.permission.ACCESS_FINE_LOCATION,
                    android.Manifest.permission.ACCESS_COARSE_LOCATION
            );

    @Test
    public void testButtonClickShowsShoppingList() throws InterruptedException {
        onView(withId(R.id.btnShoppingList))
                .perform(click());

        onView(withId(R.id.shoppingListView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testLeafletFileSavedToDownloads() {
        onView(withId(R.id.btnDownloadPdf)).perform(click());

        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir, "gazetka.pdf");

        assertTrue("File wasn't saved!", file.exists());
    }

    @Test
    public void testButtonClickShowsFavoriteStoreFragment() {
        onView(withId(R.id.navigation_favorite_store))
                .perform(click());

        onView(withId(R.id.mapView))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testButtonClickShowsShakeomatWithoutLogIn() {
        onView(withId(R.id.navigation_shakeomat))
                .perform(click());

        onView(withId(R.id.text_shakeomat))
                .check(ViewAssertions.matches(ViewMatchers.withText("Aby otrzymać promocję musisz się zalogować!")));
    }

    @Test
    public void testButtonClickShowsAccount() {
        onView(withId(R.id.navigation_account))
                .perform(click());

        onView(withId(R.id.loginButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }

    @Test
    public void testButtonClickShows() {
        onView(withId(R.id.navigation_account))
                .perform(click());

        onView(withId(R.id.loginButton))
                .check(ViewAssertions.matches(ViewMatchers.isDisplayed()));
    }
}


