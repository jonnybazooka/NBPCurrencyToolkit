# NBPCurrencyToolkit
Desktop application originally developed as an exercise in external APIs usage and JSON manipulation. While more functionalities were added, I took some time to study neural networks architecture and implemented one into this application. It became it's main feature.

## MongoDB
All internal operations of this application are conducted on a data stored in a local MongoDB database. In order to enjoy all functionalities it is required to have MongoDB pre-installed. Application will try to create new database named ```nbpCurrencyDatabase```. It will not ask for any credentials so free access is also a prerequisite. A free version can be downloaded [here](https://www.mongodb.com/download-center/community).

## NBP's API GUI
A simple GUI that allows user to choose one currency from 5 available (GBP, USD, CHF, JPY, EUR) and enter the period for which the records will be obtained. Application constructs a proper query and sends a request upon clicking ```Execute Query``` button.

Application is able to circumvent some of the limitations imposed by the API. For example after such request http://api.nbp.pl/api/exchangerates/rates/a/usd/2016-01-01/2018-12-31/?format=json the server's response will be ```400 BadRequest - Przekroczony limit 367 dni / Limit of 367 days has been exceeded```. This application will break such long requests into multiple smaller ones and send them one after another. The whole process is thread-safe. Result of a query is subsequently formatted. 

```Save Results to Database``` button saves query's results. It is important to do this since other features of this application relay database to get necessary data. Example:

![First View](https://i.ibb.co/fn7Ds1P/NBP-First-View.png)

## Database View
This panel allows user to get data from the database and view them in table format. User can select specific currencies and time period. If he wishes he can also delete selected data. Example:

![Database View](https://i.ibb.co/r5jF2cW/NBP-Second-View.png)

## Statistical Algorithms
This panel implements basic statistical algorithm for data analysis. The column on the right side shows basic statistics available for single currency. The column on the left side shows correlation statistics. To display them user have to select two currencies. BigDecimal class was used for greater precision (and exercise purposes mostly). Examples:

![Basic Stats](https://i.ibb.co/Q8tG1R3/NBP-Basic-Stats.png)

![Correlation Stats](https://i.ibb.co/9HdjHZh/NBP-Correlation.png)

## Neural Network
This is the most advanced feature of this application. This neural network is using data about currency rates changes from selected period and trains itself for the purpose of predicting changes in another period. User have some freedom in setting network's parameters and can choose learning set of data and testing set of data. All the data have to be already saved into database. User can select one of four currencies (EUR, USD, GBP, CHF) for which the network will be callibrated. JPY is not available for reasons explained further below.

### How it works?
Neural networks aren't usually programmed in Java. There are languages better suited for the task. Fortunately internet's resources are immense and the architecture itself is of mathematical nature.

**Layers**

This network has 3 to 6 layers. The first one and the last one are fixed. The hidden layers (middle ones) are customizable by the user. The input layer has 8 neurons. Those neurons will contain information about all currency rate changes from last two trading days. It includes the currency that is out main focus. The output layer has only two neurons, let's call them neuron_0 and neuron_1. Their individual value is always between 0 and 1. Their sum is always 1.

Hidden layers of neurons is where the magic takes place. There are whole books about how to set up those layers for specific tasks. This layer put through a process of training will be capable of finding non-linear dependencies between inputs and writing them as arrays of numbers.

**Activation Function**

Every neuron takes some input and sends it forward. This input is transformed by a function called 'activation fucntion'. There are six commonly used used functions and this neural network is using [sigmoid function](https://en.wikipedia.org/wiki/Sigmoid_function). It is the part of the algorythm that is mostly responsible for capturing any non-linear correlations.

**Bias**

Evey neuron layer has something called bias. You can think of it as an additional neuron in each layer with weights attached to every other neuron in that layer. It acts as some constant value that modifies output value of each neuron from it's layer. While activation function is responsible for finding non-linear behavoiur of our currency, the bias is there to find linear behavoiur.

**Weights**

Every neuron is connected to every other neuron in the next layer. In a human brain we would call those connections 'a synapse'. Here we call them 'weights'. A weight's purpose is to multiply the neuron's output by it's own value. You can imagine that today's value of British Pound can be somehow dependant on yesterday's drop of US Dollar exchange rate, but changes of CHF can have very little meaning. This simple explanation gets more complicated when we delve deeper into hidden layers as they cannot be so simply interpreted into our language. Still weights are responsible for the training process where they will be constantly adjusted with every iteration. Weights are initially set up randomly on purpose, so it is possible that 2 neural networks with 2 identical initial setups after identical training will give different results.

**Backpropagation Algorithm**

This algorithm is all about training. My initial desire was to create my own algorithm with a more random nature, but after some heavy research on the subject I decided to implement this one. During training the whole network's result will be compared to expected result and if the network failed it's prediction a blame will be assigned. The backpropagation algorithm is responsible for assigning this blame in justified manner and modifying proper weights. It is important to note that weight adjustments will be taken in many small steps, so I recommend no less than 150000 iterations of training process should be used.

**Testing**

After the whole training process it's time to find out if our network is worth anything. It's time for testing. Testing set of data should always be a different than a training set. As was said before our network will have two output neurons. If output_0 will have higher value than neuron_1 the network will predict a rise of our currency rate. Otherwise it will predict a drop. Testing set of data will contain real-life currency changes and those will be compared to our network's predictions to assess it's efficiency.

### Limitations

There is no standardization of input data (or normalization - it's quite differnt process if you ask statistics expert). There are few reason for this. First of all, the activation function is able to handle input data quite well in raw form as long as we won't include JPY there. The typical range of exchange rate changes are similar for all four input currencies. Secondly - it would be another operation that would have to be conducted in every iteration of training process, and that would be slowing whole network down. Lastly - there are many ways to standardize input data, and that would require additional research - something I didn't have time to do back then.

Another limitation ot this application is lack of possibility to use a neural network to predict tomorrow's currency rate change. It is so on purpose, as it would be very easy to implement such functionality. It was never my intention to use it for real-life trading or distribute it commercially. Forex is a very complex market and it would be naive to think that such simplistic approach could be successful. It should be treated strictly as a student's project.

### Modularity

The code for the neural network is contained in one package and is highly modular. It can be copied into other projects and put to use with very little effort. When doing so a few notes should be taken.
* Activation function is hardcoded - it should be replaced with appropriate fuction for given project.
* Initial weights are set up randomly, but within certain bounds. Those bounds are hardcoded in ```Network``` class constructor.
* Learning rate for a network as another hardcoded constant, expressed by variable called 'eta'. ```Network``` class, line 67. Adjusting this value may be necessary, but if it gets too high the network may never reach it's global minimum or miss it completely.

### Step by step tutorial

First step is to choose a currency for which our network will train.

![Currency Choice](https://i.ibb.co/Mggh966/NBP-neural-Network1.png)

Next we enter periods for training data set and testing data set. All data should be already saved into MongoDB database.

![Dates Setup](https://i.ibb.co/RCdrJdp/NBP-neural-Network2.png)

Then we use sliders to set up hidden layers

![Layers Setup](https://i.ibb.co/QfLDLXL/NBP-neural-Network3.png)

Then we set up training parameters. ```Number of cycles``` parameters is responsible for the number of iterations in which training will be completed. ```Batch size``` how many samples from training set should be chosen randomly for each iteration. It cannot exceed training data set size, but if it gets too low then training results will get more random.

![Training Setup](https://i.ibb.co/Hg5xw5R/NBP-neural-Network4.png)

We are almost there. Next step is hitting 3 buttons: ```Load Data for Training```, ```Load Data for Testing``` and ```Create New Neural Network```. Then at last we can hit ```Train``` button. Depending on our parameters and our machine processing power it will take some time. Application will tell us when it is finished.

![Training](https://i.ibb.co/Y392v1j/NBP-neural-Network5.png)

In the end we can test our network against testing set of data to see if it's any good. For every entry in our testing data set application will display real currency change and our network's prediction (with output neurons values - the closer neuron_0 is to 1, the higher certainty that currency rate will rise). At the end it will diplay statistics for success rate with *high prediction certainty* set to 75%.

![Testing](https://i.ibb.co/JkVk8wt/NBP-neural-Network6.png)

As we can see our network isn't very good. Probably with more training cycles or another layer setup we would find more success. But if we decide that this network is promissing we can save it to *.txt* file or our MongoDB in JSON format. All we have to do is enter a name for our network and press ```Save``` button.

![Save Network](https://i.ibb.co/zHykMX6/NBP-neural-Network7.png)

We can load our network later for further training or use in the same way by entering it's name and pressing appropriate ```Load``` button.

## Release

File with application is available [here](https://1drv.ms/u/s!AgTphPW2o3ZIgP802Gj3j8JVAIWuyg?e=UAo0E9).
