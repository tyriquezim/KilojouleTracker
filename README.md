This is an Android application that tracks a user's meals and allows them to set their energy and macronutrient goals. 
When the user has a meal, they simply need to enter the name of the food item and the weight of the serving in grams. 
The application will then query a publicly available food database using their API to return information related to the nutritional value of the meal.
Users can also take a photo of their meal and save it to their phone and can also view a list of all their logged meals to date along with their photos.
The application makes use of Android Room to achieve app persistence.
It uses view models to maintain state during configuration changes and utilises Retrofit to retrieve data from the Nutrionix API.
The application also uses implicit intents to launch the device's camera application to take photos of the meal and stores the result in internal storage.
The application also utilises Jetpack Compose, Google's declarative UI toolkit to build its user interface instead of the traditional Views framework.
