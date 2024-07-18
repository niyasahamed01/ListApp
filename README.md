**ListingApp**

**Functionality
The app's functionality includes:**

Fetch a list of images and names from list api (https://randomuser.me/) and show them in RecyclerView using StaggeredGrid Layout Manager.

User can view the list of images and names using smooth infinite scroll using paging api.

When an List Item is selected from RecyclerView it will load the detail page.

The  List is cached into local DB, so the list of images and names are available offline.

There also wheather api, we can watch the exact wheather condition along with lat and long with the correct attitude and along with user mobile location.

And also search the list.

**Architecture**

The app uses clean architecture with MVVM(Model View View Model) design pattern. MVVM provides better separation of concern, easier testing, Live data & lifecycle awareness, etc.

**UI**

MainActivity.kt - Initial screen. Shows a list of images and names.


**Model**

Model is generated from JSON data into a Kotlin data class. In addition, entity class has been added for room database.

**ViewModel**

EmployeeViewModel.kt

Used for fetching images and names & update flow using paging data soruce.

**Dependency Injection**

The app uses Dagger-hilt as a dependency injection library.

The DiModules.kt class provides Singleton reference for Retrofit, OkHttpClient, Repository etc.

**Network**

The network layer is composed of Repository, ApiService. EmployeeInterface - Is an interface containing the suspend functions for retrofit API call.

EmployeeRemoteMediator - Holds the definition of the remote/local repository call. 

**Data Binding Library**

The Data Binding Library is a support library that allows you to bind UI components in your layouts to data sources in your app using a declarative format rather than programmatically.

**Glide** 

An image loading and caching library for Android focused on smooth scrolling.

**Retrofit**

Retrofit to make api calls to an HTTP web service.

**Navigation**

The Navigation Architecture Component helps you easily implement common, but complex navigation requirements, while also helping you visualize your app's navigation flow. The library provides a number of benefits, including:

* Handling fragment transactions.
* Handling up and back correctly by default.
* Provides defaults for animations and transitions.

**Coroutines** 

For managing long running or network tasks off the main thread.

**GSON** 

A converter for JSON Serialization.

**LiveData** 

Objects which can be observed by UI about changes.

**ViewModel** 

Stores and manages UI related data in a lifecycle aware way.

![media_20240718_124800_6772145024339646945](https://github.com/user-attachments/assets/d909ae3f-2cea-4e21-a1fc-679911cc63f7)
![media_20240718_124800_6379028852611328907](https://github.com/user-attachments/assets/e04f22f8-a66e-4319-89e9-71a89128e71b)
![media_20240718_124800_6054695211433381593](https://github.com/user-attachments/assets/1589c295-2aea-4663-a90f-58ec9f7ab065)




