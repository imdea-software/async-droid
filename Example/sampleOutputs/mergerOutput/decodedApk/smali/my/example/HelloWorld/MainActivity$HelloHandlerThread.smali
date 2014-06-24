.class Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;
.super Landroid/os/HandlerThread;
.source "MainActivity.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lmy/example/HelloWorld/MainActivity;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0xa
    name = "HelloHandlerThread"
.end annotation


# instance fields
.field mHandler:Landroid/os/Handler;


# direct methods
.method constructor <init>(Ljava/lang/String;)V
    .locals 2
    .parameter "name"

    .prologue
    .line 166
    invoke-direct {p0, p1}, Landroid/os/HandlerThread;-><init>(Ljava/lang/String;)V

    .line 163
    const/4 v0, 0x0

    iput-object v0, p0, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->mHandler:Landroid/os/Handler;

    .line 167
    invoke-virtual {p0}, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->start()V

    .line 168
    new-instance v0, Landroid/os/Handler;

    invoke-virtual {p0}, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->getLooper()Landroid/os/Looper;

    move-result-object v1

    invoke-direct {v0, v1}, Landroid/os/Handler;-><init>(Landroid/os/Looper;)V

    iput-object v0, p0, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->mHandler:Landroid/os/Handler;

    .line 169
    return-void
.end method


# virtual methods
.method postSleepAndPrint()V
    .locals 2

    .prologue
    .line 172
    iget-object v0, p0, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;->mHandler:Landroid/os/Handler;

    new-instance v1, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread$1;

    invoke-direct {v1, p0}, Lmy/example/HelloWorld/MainActivity$HelloHandlerThread$1;-><init>(Lmy/example/HelloWorld/MainActivity$HelloHandlerThread;)V

    invoke-virtual {v0, v1}, Landroid/os/Handler;->post(Ljava/lang/Runnable;)Z

    .line 184
    return-void
.end method
