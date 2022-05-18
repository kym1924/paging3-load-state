<div>
<img src="https://img.shields.io/badge/Android-3DDC84?style=flat&logo=Android&logoColor=white" />
<img src="https://img.shields.io/badge/Kotlin-7F52FF?style=flat&logo=Kotlin&logoColor=white" />
<img src="https://img.shields.io/badge/writer-kym1924-yellow?&style=flat&logo=Android"/>
</div>

# Paging 3 - LoadState
Blog Search using *Paging 3 Library* [(and Kakao API)](https://developers.kakao.com/docs/latest/ko/daum-search/common).
<br><br>
<div>
<img width=360 height=760 src="images/image_loading.png"/>
<img width=360 height=760 src="images/image_not_loading.png"/>
</div>

#### 1. Manage and present loading states
*It is important to show the user the loading status.*

If a user requests data from the server and there is no response until the data is loaded,
- It can be frustrating for the user.

*The state of the data load request is closely related to the UI.*
- For example, when loading, *Loading Views* are shown.
- and when loading is completed, *Loading Views* are hidden and the result is shown.

To do this, the Paging library manages the load request state of data.
- through the `LoadState` class.
<br>

#### 1-1. LoadState.NotLoading
- This is when the load operation is finished and there are no errors.
- This contains an *endOfPaginationReached* variable that indicates whether the end of the data set has been reached.
```kotlin
public class NotLoading(
    endOfPaginationReached: Boolean
) : LoadState(endOfPaginationReached) {
    override fun toString(): String {
        return "NotLoading(endOfPaginationReached=$endOfPaginationReached)"
    }

    override fun equals(other: Any?): Boolean {
        return other is NotLoading &&
            endOfPaginationReached == other.endOfPaginationReached
    }

    override fun hashCode(): Int {
        return endOfPaginationReached.hashCode()
    }

    internal companion object {
        internal val Complete = NotLoading(endOfPaginationReached = true)
        internal val Incomplete = NotLoading(endOfPaginationReached = false)
    }
}
```
<br>

#### 1-2. LoadState.Loading
- This is when the load operation is in progress.
```kotlin
public object Loading : LoadState(false) {
    override fun toString(): String {
        return "Loading(endOfPaginationReached=$endOfPaginationReached)"
    }

    override fun equals(other: Any?): Boolean {
        return other is Loading &&
            endOfPaginationReached == other.endOfPaginationReached
    }

    override fun hashCode(): Int {
        return endOfPaginationReached.hashCode()
    }
}
```
<br>

#### 1-3.LoadState.Error
- This is when the load operation hit an error.
```kotlin
public class Error(public val error: Throwable) : LoadState(false) {
    override fun equals(other: Any?): Boolean {
    	return other is LoadState.Error && 
            endOfPaginationReached == other.endOfPaginationReached &&
            error == other.error
    }

    override fun hashCode(): Int {
        return endOfPaginationReached.hashCode() + error.hashCode()
    }

    override fun toString(): String {
        return "Error(endOfPaginationReached=$endOfPaginationReached, error=$error)"
    }
}
```
<br>

#### 1-4. CombinedLoadStates
- It is a Collection of LoadState.
- It has *refresh*, *prepend*, *append*, and LoadStates for *PagingSource* and *RemoteMediator*.
```kotlin
public class CombinedLoadStates (
    public val refresh: LoadState,
    public val prepend: LoadState,
    public val append: LoadState,
    public val source: LoadStates,
    public val mediator: LoadStates? = null
)
```
<br>

#### 2. How to access LoadState
There are two ways to get the load status in the UI.
- Collect the `loadStateFlow` of *the PagingDataAdapter*.
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        adapter.loadStateFlow.collect { loadState ->
            // TODO : What to do with LoadState.
        }
    }
}
```

- Use `addLoadStateListener()` of *the PagingDataAdapter*.
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        adapter.addLoadStateListener { loadState ->
            // TODO : What to do with LoadState.
        }
    }
}
```
<br>

#### 3. PagingDataAdapter
- `PagingDataAdapter` declares *AsyncPagingDataDiffer*.
- When accessing the `loadStateFlow` or `addLoadStateListener()`
  - It refers to the one declared in this *differ(AsyncPagingDataDiffer)*.
```kotlin
abstract class PagingDataAdapter<T: Any, VH: RecyclerView.ViewHolder> @JvmOverloads constructor(
    diffCallback: DiffUtil.ItemCallback<T>,
    mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    workerDispatcher: CoroutineDispatcher = Dispatchers.Default
): RecyclerView.Adapter<VH>() {
    ...
    private val differ = AsyncPagingDataDiffer(
    	diffCallback = diffCallback,
        updateCallback = AdapterListUpdateCallback(this),
        mainDispatcher = mainDispatcher,
        workerDispatcher = workerDispatcher
    )
    ...
    val loadStateFlow: Flow<CombinedLoadStates> = differ.loadStateFlow
    ...
    fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
        differ.addLoadStateListener(listener)
    }
    ...
}
```
<br>

#### 3-1. AsyncPagingDataDiffer
- `AsyncPagingDatDiffer` declares *PagingDataDiffer* object.
- When accessing the `loadStateFlow` or `addLoadStateListener()`
  - It refers to the one declared in this *differBase(PagingDataDiffer)*.
```kotlin
class AsyncPagingDatDiffer<T: Any> @JvmOverloads constructor(
    private val diffCallback: DiffUtil.ItemCallback<T>,
    private val updateCallback: ListUpdateCallback,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
    private val workerDispatcher: CoroutineDispatcher = Dispatchers.Default
) {
    ...
    private val differBase = object : PagingDataDiffer<T>(differCallback, mainDispatcher) {
        ...
    }
    ...
    val loadStateFlow: Flow<CombinedLoadStates> = differBase.loadStateFlow
    ...
    fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
        differBase.addLoadStateListener(listener)
    }
    ...
}
```
<br>

#### 3-2. PagingDataDiffer
- `PagingDataDiffer` declares *MutableCombinedLoadStateCollection*.
- When accessing the `loadStateFlow` or `addLoadStateListener()`
  - It refers to the one declared in this *combinedLoadStatesCollection(MutableCombinedLoadStateCollection)*.
```kotlin
public abstract class PagingDataDiffer<T: Any> (
    private val differCallback: DifferCallback,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main
) {
    ...
    private val combinedLoadStatesCollection = MutableCombinedLoadStateCollection()
    ...
    public val loadStateFlow: Flow<CombinedLoadStates> = combinedLoadStatesCollection.flow
    ...
    public fun addLoadStateListener(listener: (CombinedLoadStates) -> Unit) {
        combinedLoadStatesCollection.addListener(listener)
    }
    ...
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    public interface DifferCallback {
    	public fun onChanged(position: Int, count: Int)
    	public fun onInserted(position: Int, count: Int)
    	public fun onRemoved(position: Int, count: Int)
    }
    ...
}
```
<br>

#### 3-3. MutableCombinedLoadStateCollection
```kotlin
internal class MutableCombinedLoadStateCollection {
    ...
    private val listeners = CopyOnWriteArrayList<(CombinedLoadState) -> Unit>()
    ...
    private val _stateFlow = MutableStateFlow<CombinedLoadStates?>(null)
    val flow: Flow<CombinedLoadStates> = _stateFlow.filterNotNull()
    ...
    fun addListener(listener: (CombinedLoadStates) -> Unit) {
        listeners.add(listener)
        snapshot()?.also { listener(it) }
    }
}
```
<br>

#### 4. _stateFlow
- When is _stateFlow set to a value?
- When will this flow emit a value?
<br>

#### 4-1. submitData
When calling submitData of the PagingDataAdapter, the following process is followed.
*PagingDataAdapter.submitData()* - *AsyncPagingDataDiffer.submitData()* - *PagingDataDiffer.collectFrom()*.
```kotlin
//PagingDataAdapter.submitData(pagingData)
suspend fun submitData(pagingData: PagingData<T>){
    differ.submitData(pagingData)  
}

//AsyncPagingDataDiffer.submitData(pagingData)
suspend fun submitData(pagingData: PagingData<T>){
    submitDataId.incrementAndGet()
    differBase.collectFrom(pagingData)  
}

//PagingDataDiffer.collectFrom(pagingData)
public suspend fun collectFrom(pagingData: PagingData<T>) {
    collectFromRunner.runInIsolation {
        receiver = pagingData.receiver
    }
    // TODO: Validate only empty pages between separator pages and its dependent pages.
    pagingData.flow.collect { event ->
    	withContext(mainDispatcher) {
            if (event is Insert && event.loadType == REFRESH) {
                ...
                // Dispatch LoadState updates as soon as we are done diffing, but after setting presenter.
                dispatchLoadStates(event.sourceLoadStates, event.mediatorLoadStates)
                ...
            } else {
                ...
                presenter.processEvent(event, processPageEventCallback)
                ...
            }
        }
}
```
- As a result, it calls `dispatchLoadStates()` or `presenter.processEvent()`.
<br>

#### 4-2. PagePresenter
- `PagePresenter<T>` is declared in *PagingDataDiffer*.
```kotlin
public abstract class PagingDataDiffer<T: Any>(
    ...
) {
    private val presenter: PagePresenter<T> = PagePresenter.initial()
}

//PagePresenter
internal class PagePresenter<T: Any>(
    ...
): NullPaddedList<T> {
    ...
    internal companion object {
        private val INITIAL = PagePresenter<Any>(PageEvent.Insert.EMPTY_REFRESH_LOCAL)
        
        @Suppress("UNCHECKED_CAST", "SyntheticAccessor")
        internal fun <T : Any> initial(): PagePresenter<T> = INITIAL as PagePresenter<T>
    }
}
```
<br>

#### 4-3. processEvent()
- It calls the `processEvent()` method of *PagePresenter*.
```kotlin
//PagePresenter.processEvent()
fun processEvent(pageEvent: PageEvent<T>, callback: ProcessPageEventCallback) {
    when(pageEvent) {
        is PageEvent.Insert -> insertPage(pageEvent, callback)
        is PageEvent.Drop -> dropPages(pageEvent, callback)
        is PageEvent.LoadStateUpdate -> {
            callback.onStateUpdate(
                source = pageEvent.source,
                mediator = pageEvent.mediator
            )
        }
    }
}
```

- If the PageEvent value is *PageEvent.Insert*, `insertPage()` is called.
- If the PageEvent value is *PageEvent.Drop*, `dropPages()` is called.
- If the PageEvent value is *PageEvent.LoadStateUpdate*, `onStateUpdate()` of the callback is called.

```kotlin
private fun insertPage(insert: PageEvent.Insert<T>, callback: ProcessPageEventCallback) {
    ...
    callback.onStateUpdate(
    	source = insert.sourceLoadStates,
        mediator = insert.mediatorLoadStates
    )
}

private fun dropPages(drop: PageEvent.Drop<T>, callback: ProcessPageEventCallback) {
    if(drop.loadType == PREPEND) {
        ...
        // Dropping from prepend direction implies NotLoading(endOfPaginationReached = false).
        callback.onStateUpdate(
        	loadType = PREPEND,
        	fromMediator = false,
        	loadState = NotLoading.Incomplete
        )
    } else {
        // Dropping from append direction implies NotLoading(endOfPaginationReached = false).
            callback.onStateUpdate(
            loadType = APPEND,
            fromMediator = false,
            loadState = NotLoading.Incomplete
        )
    }
}
```
- `insertPage` and `dropPages` also call `onStateUpdate()` at the end.
<br>

#### 4-4.ProcessPageEventCallback
- Callback to communicate events from *PagePresenter* to *PagingDataDiffer*.
- It has an `onStateUpdate()` method.
- onStateUpdate(loadType: LoadType, fromMediator: Boolean, loadState: LoadState)
- onStateUpdate(source: LoadStates, mediator: LoadStates?)

```kotlin
internal interface ProcessPageEventCallback {
    fun onChanged(position: Int, count: Int)
    fun onInserted(position: Int, count: Int)
    fun onRemoved(position: Int, count: Int)
    fun onStateUpdate(loadType: LoadType, fromMediator: Boolean, loadState: LoadState)
    fun onStateUpdate(source: LoadStates, mediator: LoadStates?)
}
```

- PagingDataDiffer implements this ProcessPageEventCallback interface.

```kotlin
// PagingDataDiffer
private val processPageEventCallback = object : ProcessPageEventCallback {
    override fun onChanged(position: Int, count: Int) {
        differCallback.onChanged(position, count)
    }
    override fun onInserted(position: Int, count: Int) {
        differCallback.onInserted(position, count)
    }
    override fun onRemoved(position: Int, count: Int) {
        differCallback.onRemoved(position, count)
    }
    override fun onStateUpdate(source: LoadStates, mediator: LoadStates?) {
        dispatchLoadStates(source, mediator)    
    }
    override fun onStateUpdate(
        loadType: LoadType, 
        fromMediator: Boolean, 
        loadState: LoadState
    ) {
        val currentLoadState = combinedLoadStatesCollection.get(loadType, fromMediator)
        
        if(currentLoadState == loadState) return
        
        combinedLoadStatesCollection.set(loadType, fromMediator, loadState)
    }
}
```

- onStateUpdate(source: LoadStates, mediator: LoadStates?) calls `dispatchLoadStates()`.
- onStateUpdate(loadType: LoadType, fromMediator: Boolean, loadState: LoadState) calls  `set()`.
<br>

#### 4-5. dispatchLoadStates()
- If the received *LoadStates* value is different from the current value `set()` is called.
```kotlin
//PagingDataDiffer.dispatchLoadStates()
internal fun dispatchLoadStates(source: LoadStates, mediator: LoadStates?) {
    // No change, skip update + dispatch.
    if (combinedLoadStatesCollections.source == source && combinedLoadStatesCollections.mediator == mediator) {
        return
    }
    combinedLoadStatesCollections.set(sourceLoadStates = source, remoteLoadStates = mediator)
}
```
<br>

#### 4-6. MutableCombinedLoadStateCollection.set()
- This is a method that specifies *the source* and *mediator* values of MutableCombinedLoadStateCollection.
- And call `updateHelperStatesAndDispatch()`.

```kotlin
internal class MutableCombinedLoadStateCollection {
    ...
    private var refresh: LoadState = NotLoading.Incomplete
    private var prepend: LoadState = NotLoading.Incomplete
    private var append: LoadState = NotLoading.Incomplete
    var source: LoadStates = LoadStates.IDLE
        private set
    var mediator: LoadStates? = null
        private set
    ...
    fun set(sourceLoadStates: LoadStates, remoteLoadStates: LoadStates?) {
        isInitialized = true
        source = sourceLoadStates
        mediator = remoteLoadStates
        updateHelperStatesAndDispatch()
    }

    fun set(type: LoadType, remote: Boolean, state: LoadState): Boolean {
        isInitialized = true
        val didChange = if (remote) {
            val lastMediator = mediator
            mediator = (mediator ?: LoadStates.IDLE).modifyState(type, state)
            mediator != lastMediator
        } else {
            val lastSource = source
            source = source.modifyState(type, state)
            source != lastSource
        }

        updateHelperStatesAndDispatch()
        return didChange
    }
    ...
}
```
<br>

#### 4-7. updateHelperStatesAndDispatch()
- This is a method that specifies *the refresh*, *prepend* and *append* values.
- And call `snapshot()`.
```kotlin
private fun updateHelperStatesAndDispatch() {
    refresh = computeHelperState(
        previousState = refresh,
        sourceRefreshState = source.refresh,
        sourceState = source.refresh,    
        remoteState = mediator?.refresh
    )
    prepend = computeHelperState(
        previousState = prepend,
        sourceRefreshState = source.refresh,
        sourceState = source.prepend,
        remoteState = mediator?.prepend
    )
    append = computeHelperState(
        previousState = append,
        sourceRefreshState = source.refresh,
        sourceState = source.append,
        remoteState = mediator?.append
    )

    val snapshot = snapshot()
    if (snapshot != null) {
        _stateFlow.value = snapshot
        listeners.forEach { it(snapshot) }
    }
}
```
<br>

#### 4-8. snapshot()
- Returns null if isInitialized is false, otherwise returns *CombinedLoadStates*.
- If not null (isInitialized isn't false) , *set as the value of _stateFlow*.
- listeners also run.
```kotlin
private fun snapshot(): CombinedLoadStates? = when {
    !isInitialized -> null
    else -> CombinedLoadStates(
    	refresh = refresh,
        prepend = prepend,
        append = append,
        source = source,
        mediator = mediator
    )
}
```
<br>

#### 5. LoadStateAdapter
- `LoadStateAdapter` allows to *display the loading state* in a RecyclerView that displays paged data.
<div>
<img width=360 height=760 src="images/image_footer_loading.png"/>
<img width=360 height=760 src="images/image_footer_error.png"/>
</div>

```kotlin
abstract class LoadStateAdapter<VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {
    var loadState: LoadState = LoadState.NotLoading(endOfPaginationReached = false)
        set(loadState) {
            if (field != loadState) {
                val oldItem = displayLoadStateAsItem(field)
                val newItem = displayLoadStateAsItem(loadState)

                if (oldItem && !newItem) {
                    notifyItemRemoved(0)
                } else if (newItem && !oldItem) {
                    notifyItemInserted(0)
                } else if (oldItem && newItem) {
                    notifyItemChanged(0)
                }
                field = loadState
            }
        }
    ...
    open fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        return loadState is LoadState.Loading || loadState is LoadState.Error
    }
}
```

- Unlike RecyclerView.Adapter, *LoadState* is passed to *onCreateViewHolder()* and *onBindViewHolder()*.

```kotlin
final override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
    return onCreateViewHolder(parent, loadState)
}

final override fun onBindViewHolder(holder: VH, position: Int) {
    onBindViewHolder(holder, loadState)
}

final override fun getItemViewType(position: Int): Int = getStateViewType(loadState)

final override fun getItemCount(): Int = if (displayLoadStateAsItem(loadState)) 1 else 0

abstract fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): VH

abstract fun onBindViewHolder(holder: VH, loadState: LoadState)
```
<br>

#### 5-1. PagingDataAdapter.retry()
- In case an *Error occurs during loading*, the `retry` method of *PagingDataAdapter* is passed.
```kotlin
class BlogLoadAdapter(private val retry: View.OnClickListener) : LoadStateAdapter<BlogLoadAdapter.BlogLoadViewHolder>() {
    ...
}
```
<br>

```kotlin
val headerAdapter = BlogLoadAdapter { adapter.retry() }
val footerAdapter = BlogLoadAdapter { adapter.retry() }
```
<br>

#### 5-2. withLoadStateHeaderAndFooter
- This method takes a `LoadStateAdapter` and returns a `ConcatAdapter`.
```kotlin
val adapterWithLoadState = adapter.withLoadStateHeaderAndFooter(
    header = headerAdapter,
    footer = footerAdapter
)
```
<br>

- To show the loading status in *header* and *footer*, use `withLoadStateHeaderAndFooter()`.
- header loadState set *loadStates.prepend*.
- footer loadState set *loadStates.append*.
```kotlin
fun withLoadStateHeaderAndFooter(
    header: LoadStateAdapter<*>,
    footer: LoadStateAdapter<*>
): ConcatAdapter {
    addLoadStateListener { loadStates ->
        header.loadState = loadStates.prepend
        footer.loadState = loadStates.append
    }
    return ConcatAdapter(header, this, footer)
}
```
<br>

- To show the loading status in *footer*, use `withLoadStateFooter()`.
```kotlin
fun withLoadStateFooter(
    footer: LoadStateAdapter<*>
): ConcatAdapter {
    addLoadStateListener { loadStates ->
        footer.loadState = loadStates.append
    }
    return ConcatAdapter(this, footer)
}
```
- When *additional data loads are in progress*, append becomes `Loading`.
- When *loading is complete*, append becomes `NotLoading`.

```
I/okhttp.OkHttpClient: --> GET https://dapi.kakao.com/v2/search/blog?query=%EC%82%AC%EA%B1%B4%EC%9D%98%EC%A7%80%ED%8F%89%EC%84%A0&sort=recency&page=4&size=15

CombinedLoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=NotLoading(endOfPaginationReached=true), append=Loading(endOfPaginationReached=false), source=LoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=NotLoading(endOfPaginationReached=true), append=Loading(endOfPaginationReached=false)), mediator=null)

I/okhttp.OkHttpClient: <-- 200 OK https://dapi.kakao.com/v2/search/blog?query=%EC%82%AC%EA%B1%B4%EC%9D%98%EC%A7%80%ED%8F%89%EC%84%A0&sort=recency&page=4&size=15 (72ms, unknown-length body)

CombinedLoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=NotLoading(endOfPaginationReached=true), append=NotLoading(endOfPaginationReached=false), source=LoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=NotLoading(endOfPaginationReached=true), append=NotLoading(endOfPaginationReached=false)), mediator=null)
```
<br>

- To show the loading status in *header*, use `withLoadStateHeader()`.
```kotlin
fun withLoadStateHeader(
    header: LoadStateAdapter<*>
): ConcatAdapter {
    addLoadStateListener { loadStates ->
        header.loadState = loadStates.prepend
    }
    return ConcatAdapter(header, this)
}
```
- When *previous data loads are in progress*, prepend becomes `Loading`.
- When *loading is complete*, append becomes `NotLoading`.

```
I/okhttp.OkHttpClient: --> GET https://dapi.kakao.com/v2/search/blog?query=%EC%82%AC%EA%B1%B4%EC%9D%98%EC%A7%80%ED%8F%89%EC%84%A0&sort=recency&page=3&size=15

CombinedLoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=Loading(endOfPaginationReached=false), append=NotLoading(endOfPaginationReached=false), source=LoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=Loading(endOfPaginationReached=false), append=NotLoading(endOfPaginationReached=false)), mediator=null)

I/okhttp.OkHttpClient: <-- 200 OK https://dapi.kakao.com/v2/search/blog?query=%EC%82%AC%EA%B1%B4%EC%9D%98%EC%A7%80%ED%8F%89%EC%84%A0&sort=recency&page=3&size=15 (55ms, unknown-length body)

CombinedLoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=NotLoading(endOfPaginationReached=false), append=NotLoading(endOfPaginationReached=false), source=LoadStates(refresh=NotLoading(endOfPaginationReached=false), prepend=NotLoading(endOfPaginationReached=false), append=NotLoading(endOfPaginationReached=false)), mediator=null)
```
<br>

#### 5-3. ViewHolder
- Create bind() in the ViewHolder and pass the loadState.
- Manage the visibility of views according to the received loadState.
```kotlin
class BlogLoadViewHolder(
    private val binding: ItemLoadStateBinding,
    private val retry: () -> Unit)
) : RecyclerView.ViewHolder(binding.root) {
    init {
        binding.tvRetry.setOnClickListener {
            retry.invoke()
        }
    }
    fun bind(loadState: LoadState) {
        binding.progressLoading.isVisible = loadState is LoadState.Loading
        binding.tvRetry.isVisible = loadState is LoadState.Error
    }
}
```
<br>

#### 6. Chain Operators on LoadState
- When viewing new search results, need to navigate to the beginning instead of keeping scrolling.
- [Flow&lt;CombinedLoadStates>](#3-3-mutablecombinedloadstatecollection) emit value to all changes in *LoadState*.
- Filter can be applied to prevent unnecessary UI updates.
```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        adapter.loadStateFlow
            .distinctUntilChangedBy { it.refresh }
            .filter { it.refresh is LoadState.NotLoading }
            .collect { binding.rvDocument.scrollToPosition(0) }
    }
}
```
<br>

##### Reference

- https://developer.android.com/topic/libraries/architecture/paging/load-state
- https://developer.android.com/codelabs/android-paging
- https://developer.android.com/reference/kotlin/androidx/paging/CombinedLoadStates
- https://developer.android.com/reference/kotlin/androidx/paging/LoadStateAdapter
- https://github.com/android/architecture-components-samples/tree/main/PagingSample
- https://github.com/android/architecture-components-samples/tree/main/PagingWithNetworkSample
- https://medium.com/androiddevelopers/fetching-data-and-binding-it-to-the-ui-in-the-mad-skills-series-cea89868b3e1
- https://www.youtube.com/watch?v=OHH_FPbrjtA
- https://github.com/facebook/shimmer-android
