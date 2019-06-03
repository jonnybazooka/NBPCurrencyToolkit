# NBPCurrencyToolkit

# This project has 4 funcionalities:
# 1 - It provides simple GUI for NBP's API (http://api.nbp.pl/) and circumvents some of API limitations. For example API dosn't allow for
#   queries longer than 367 days of data. Application will still provide results of such query by dividing it into multiple smaller
#   queries.
# 2 - It provides a MongoDB integration for data storage and furter use in application. Due to unrelated nature of data it is preferable to
#   use noSQL type of database.
# 3 - It provides some statistical algorythms for data analysis.
# 4 - Last but not least, it provides simple neuron network that can learn, and predict further currency rate changes.
