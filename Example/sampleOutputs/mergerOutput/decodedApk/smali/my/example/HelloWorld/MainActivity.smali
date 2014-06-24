.class public Lmy/example/HelloWorld/MainActivity;
.super Landroid/app/Activity;
.source "MainActivity.java"


# annotations
.annotation system Ldalvik/annotation/MemberClasses;
    value = {
        Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;,
        Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;,
        Lmy/example/HelloWorld/MainActivity$HelloThread;,
        Lmy/example/HelloWorld/MainActivity$MyHandler;,
        Lmy/example/HelloWorld/MainActivity$PlaceholderFragment;
    }
.end annotation


# instance fields
.field private UIEvents:I

.field private asyncTasks:I

.field private asyncTasksExec:I

.field private handler:Lmy/example/HelloWorld/MainActivity$MyHandler;

.field private handlerWorker:Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;

.field private looper:Landroid/os/Looper;

.field private messages:I

.field private runnables:I

.field private threads:I


# direct methods
.method public constructor <init>()V
    .locals 1

    .prologue
    const/4 v0, 0x0

    .line 19
    invoke-direct {p0}, Landroid/app/Activity;-><init>()V

    .line 25
    iput v0, p0, Lmy/example/HelloWorld/MainActivity;->UIEvents:I

    .line 26
    iput v0, p0, Lmy/example/HelloWorld/MainActivity;->messages:I

    .line 27
    iput v0, p0, Lmy/example/HelloWorld/MainActivity;->runnables:I

    .line 28
    iput v0, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasks:I

    .line 29
    iput v0, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasksExec:I

    .line 30
    iput v0, p0, Lmy/example/HelloWorld/MainActivity;->threads:I

    .line 19
    return-void
.end method


# virtual methods
.method public createAsyncTask(Landroid/view/View;)V
    .locals 5
    .parameter "view"

    .prologue
    .line 105
    const v2, 0x7f080004

    invoke-virtual {p0, v2}, Lmy/example/HelloWorld/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    .line 106
    .local v1, text:Landroid/widget/TextView;
    new-instance v2, Ljava/lang/StringBuilder;

    const-string v3, "Created #"

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v3, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasks:I

    add-int/lit8 v3, v3, 0x1

    iput v3, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasks:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 107
    new-instance v0, Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;

    const/4 v2, 0x0

    invoke-direct {v0, p0, v2}, Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;-><init>(Lmy/example/HelloWorld/MainActivity;Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;)V

    .line 108
    .local v0, asyncTask:Landroid/os/AsyncTask;,"Landroid/os/AsyncTask<Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Void;>;"
    const/4 v2, 0x1

    new-array v2, v2, [Ljava/lang/Integer;

    const/4 v3, 0x0

    iget v4, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasks:I

    invoke-static {v4}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v4

    aput-object v4, v2, v3

    invoke-virtual {v0, v2}, Landroid/os/AsyncTask;->execute([Ljava/lang/Object;)Landroid/os/AsyncTask;

    .line 109
    return-void
.end method

.method public createAsyncTaskOnExecutor(Landroid/view/View;)V
    .locals 6
    .parameter "view"

    .prologue
    .line 112
    const v2, 0x7f080005

    invoke-virtual {p0, v2}, Lmy/example/HelloWorld/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    .line 113
    .local v1, text:Landroid/widget/TextView;
    new-instance v2, Ljava/lang/StringBuilder;

    const-string v3, "Created #"

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v3, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasksExec:I

    add-int/lit8 v3, v3, 0x1

    iput v3, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasksExec:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 114
    new-instance v0, Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;

    const/4 v2, 0x0

    invoke-direct {v0, p0, v2}, Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;-><init>(Lmy/example/HelloWorld/MainActivity;Lmy/example/HelloWorld/MainActivity$HelloAsyncTask;)V

    .line 115
    .local v0, asyncTask:Landroid/os/AsyncTask;,"Landroid/os/AsyncTask<Ljava/lang/Integer;Ljava/lang/Integer;Ljava/lang/Void;>;"
    sget-object v2, Landroid/os/AsyncTask;->THREAD_POOL_EXECUTOR:Ljava/util/concurrent/Executor;

    const/4 v3, 0x1

    new-array v3, v3, [Ljava/lang/Integer;

    const/4 v4, 0x0

    iget v5, p0, Lmy/example/HelloWorld/MainActivity;->asyncTasks:I

    invoke-static {v5}, Ljava/lang/Integer;->valueOf(I)Ljava/lang/Integer;

    move-result-object v5

    aput-object v5, v3, v4

    invoke-virtual {v0, v2, v3}, Landroid/os/AsyncTask;->executeOnExecutor(Ljava/util/concurrent/Executor;[Ljava/lang/Object;)Landroid/os/AsyncTask;

    .line 116
    return-void
.end method

.method public createThread(Landroid/view/View;)V
    .locals 4
    .parameter "view"

    .prologue
    .line 120
    const v2, 0x7f080006

    invoke-virtual {p0, v2}, Lmy/example/HelloWorld/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    .line 121
    .local v0, text:Landroid/widget/TextView;
    new-instance v2, Ljava/lang/StringBuilder;

    const-string v3, "Created #"

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v3, p0, Lmy/example/HelloWorld/MainActivity;->threads:I

    add-int/lit8 v3, v3, 0x1

    iput v3, p0, Lmy/example/HelloWorld/MainActivity;->threads:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v0, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 122
    new-instance v1, Lmy/example/HelloWorld/MainActivity$HelloThread;

    iget v2, p0, Lmy/example/HelloWorld/MainActivity;->threads:I

    invoke-direct {v1, p0, v2}, Lmy/example/HelloWorld/MainActivity$HelloThread;-><init>(Lmy/example/HelloWorld/MainActivity;I)V

    .line 123
    .local v1, worker:Ljava/lang/Thread;
    invoke-virtual {v1}, Ljava/lang/Thread;->start()V

    .line 124
    return-void
.end method

.method protected onCreate(Landroid/os/Bundle;)V
    .locals 3
    .parameter "savedInstanceState"

    .prologue
    .line 34
    invoke-super {p0, p1}, Landroid/app/Activity;->onCreate(Landroid/os/Bundle;)V

    .line 35
    const v0, 0x7f030001

    invoke-virtual {p0, v0}, Lmy/example/HelloWorld/MainActivity;->setContentView(I)V

    .line 37
    if-nez p1, :cond_0

    .line 38
    invoke-virtual {p0}, Lmy/example/HelloWorld/MainActivity;->getFragmentManager()Landroid/app/FragmentManager;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/FragmentManager;->beginTransaction()Landroid/app/FragmentTransaction;

    move-result-object v0

    .line 39
    const/high16 v1, 0x7f08

    new-instance v2, Lmy/example/HelloWorld/MainActivity$PlaceholderFragment;

    invoke-direct {v2}, Lmy/example/HelloWorld/MainActivity$PlaceholderFragment;-><init>()V

    invoke-virtual {v0, v1, v2}, Landroid/app/FragmentTransaction;->add(ILandroid/app/Fragment;)Landroid/app/FragmentTransaction;

    move-result-object v0

    invoke-virtual {v0}, Landroid/app/FragmentTransaction;->commit()I

    .line 43
    :cond_0
    new-instance v0, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;

    const-string v1, "HelloHandlerThread"

    invoke-direct {v0, v1}, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;-><init>(Ljava/lang/String;)V

    iput-object v0, p0, Lmy/example/HelloWorld/MainActivity;->handlerWorker:Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;

    .line 44
    iget-object v0, p0, Lmy/example/HelloWorld/MainActivity;->handlerWorker:Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;

    invoke-virtual {v0}, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->getLooper()Landroid/os/Looper;

    move-result-object v0

    iput-object v0, p0, Lmy/example/HelloWorld/MainActivity;->looper:Landroid/os/Looper;

    .line 45
    new-instance v0, Lmy/example/HelloWorld/MainActivity$MyHandler;

    iget-object v1, p0, Lmy/example/HelloWorld/MainActivity;->looper:Landroid/os/Looper;

    invoke-direct {v0, p0, v1}, Lmy/example/HelloWorld/MainActivity$MyHandler;-><init>(Lmy/example/HelloWorld/MainActivity;Landroid/os/Looper;)V

    iput-object v0, p0, Lmy/example/HelloWorld/MainActivity;->handler:Lmy/example/HelloWorld/MainActivity$MyHandler;

    .line 46
    return-void
.end method

.method public onCreateOptionsMenu(Landroid/view/Menu;)Z
    .locals 2
    .parameter "menu"

    .prologue
    .line 50
    invoke-virtual {p0}, Lmy/example/HelloWorld/MainActivity;->getMenuInflater()Landroid/view/MenuInflater;

    move-result-object v0

    const v1, 0x7f070001

    invoke-virtual {v0, v1, p1}, Landroid/view/MenuInflater;->inflate(ILandroid/view/Menu;)V

    .line 51
    const/4 v0, 0x1

    return v0
.end method

.method public onOptionsItemSelected(Landroid/view/MenuItem;)Z
    .locals 2
    .parameter "item"

    .prologue
    .line 56
    invoke-interface {p1}, Landroid/view/MenuItem;->getItemId()I

    move-result v0

    .line 57
    .local v0, id:I
    const v1, 0x7f080007

    if-ne v0, v1, :cond_0

    .line 58
    const/4 v1, 0x1

    .line 60
    :goto_0
    return v1

    :cond_0
    invoke-super {p0, p1}, Landroid/app/Activity;->onOptionsItemSelected(Landroid/view/MenuItem;)Z

    move-result v1

    goto :goto_0
.end method

.method public postRunnable(Landroid/view/View;)V
    .locals 3
    .parameter "view"

    .prologue
    .line 99
    const v1, 0x7f080003

    invoke-virtual {p0, v1}, Lmy/example/HelloWorld/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    .line 100
    .local v0, text:Landroid/widget/TextView;
    new-instance v1, Ljava/lang/StringBuilder;

    const-string v2, "Taken #"

    invoke-direct {v1, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v2, p0, Lmy/example/HelloWorld/MainActivity;->runnables:I

    add-int/lit8 v2, v2, 0x1

    iput v2, p0, Lmy/example/HelloWorld/MainActivity;->runnables:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 101
    iget-object v1, p0, Lmy/example/HelloWorld/MainActivity;->handlerWorker:Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;

    invoke-virtual {v1}, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->postSleepAndPrint()V

    .line 102
    return-void
.end method

.method public processUI(Landroid/view/View;)V
    .locals 3
    .parameter "view"

    .prologue
    .line 84
    const v1, 0x7f080001

    invoke-virtual {p0, v1}, Lmy/example/HelloWorld/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v0

    check-cast v0, Landroid/widget/TextView;

    .line 85
    .local v0, text:Landroid/widget/TextView;
    new-instance v1, Ljava/lang/StringBuilder;

    const-string v2, "Event #"

    invoke-direct {v1, v2}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v2, p0, Lmy/example/HelloWorld/MainActivity;->UIEvents:I

    add-int/lit8 v2, v2, 0x1

    iput v2, p0, Lmy/example/HelloWorld/MainActivity;->UIEvents:I

    invoke-virtual {v1, v2}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v1

    invoke-virtual {v1}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v1

    invoke-virtual {v0, v1}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 86
    return-void
.end method

.method public sendMessage(Landroid/view/View;)V
    .locals 4
    .parameter "view"

    .prologue
    .line 90
    const v2, 0x7f080002

    invoke-virtual {p0, v2}, Lmy/example/HelloWorld/MainActivity;->findViewById(I)Landroid/view/View;

    move-result-object v1

    check-cast v1, Landroid/widget/TextView;

    .line 91
    .local v1, text:Landroid/widget/TextView;
    new-instance v2, Ljava/lang/StringBuilder;

    const-string v3, "Taken #"

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v3, p0, Lmy/example/HelloWorld/MainActivity;->messages:I

    add-int/lit8 v3, v3, 0x1

    iput v3, p0, Lmy/example/HelloWorld/MainActivity;->messages:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-virtual {v1, v2}, Landroid/widget/TextView;->setText(Ljava/lang/CharSequence;)V

    .line 92
    iget-object v2, p0, Lmy/example/HelloWorld/MainActivity;->handler:Lmy/example/HelloWorld/MainActivity$MyHandler;

    invoke-virtual {v2}, Lmy/example/HelloWorld/MainActivity$MyHandler;->obtainMessage()Landroid/os/Message;

    move-result-object v0

    .line 93
    .local v0, msg:Landroid/os/Message;
    iget v2, p0, Lmy/example/HelloWorld/MainActivity;->messages:I

    iput v2, v0, Landroid/os/Message;->what:I

    .line 94
    iget-object v2, p0, Lmy/example/HelloWorld/MainActivity;->handler:Lmy/example/HelloWorld/MainActivity$MyHandler;

    invoke-virtual {v2, v0}, Lmy/example/HelloWorld/MainActivity$MyHandler;->sendMessage(Landroid/os/Message;)Z

    .line 95
    return-void
.end method
