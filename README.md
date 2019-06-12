# NBPCurrencyToolkit

# This project has 4 functionalities:
# 1 - It provides simple GUI for NBP's API (http://api.nbp.pl/) and circumvents some of API limitations. For example API doesn't allow for  queries longer than 367 days of data. Application will still provide results of such query by dividing it into multiple smaller queries.
# 2 - It provides a MongoDB integration for data storage and further use in application. Due to unrelated nature of data it is preferable to use noSQL type of database. Application will attempt to create new database named "nbpCurrencyDatabase". It is required to pre-install MongoDB with open access, since the application will not ask for credentials.
# 3 - It provides some statistical algorithms for data analysis. Some of them are used later in neural network training process.
# 4 - Last but not least, it provides simple neural network that can learn, and predict further currency rate changes. Network can be saved/loaded for further use or training. Save/Load operations can be conducted either to a file or Mongo Database.
