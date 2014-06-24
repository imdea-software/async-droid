.class Lmy/example/HelloWorld/MainActivity$HelloThread;
.super Ljava/lang/Thread;
.source "MainActivity.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingClass;
    value = Lmy/example/HelloWorld/MainActivity;
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x2
    name = "HelloThread"
.end annotation


# instance fields
.field private final id:I

.field final synthetic this$0:Lmy/example/HelloWorld/MainActivity;


# direct methods
.method public constructor <init>(Lmy/example/HelloWorld/MainActivity;I)V
    .locals 0
    .parameter
    .parameter "id"

    .prologue
    .line 130
    iput-object p1, p0, Lmy/example/HelloWorld/MainActivity$HelloThread;->this$0:Lmy/example/HelloWorld/MainActivity;

    invoke-direct {p0}, Ljava/lang/Thread;-><init>()V

    .line 131
    iput p2, p0, Lmy/example/HelloWorld/MainActivity$HelloThread;->id:I

    .line 132
    return-void
.end method


# virtual methods
.method public run()V
    .locals 5

    .prologue
    .line 136
    const-wide/16 v1, 0xbb8

    :try_start_0
    invoke-static {v1, v2}, Ljava/lang/Thread;->sleep(J)V
    :try_end_0
    .catch Ljava/lang/InterruptedException; {:try_start_0 .. :try_end_0} :catch_0

    .line 141
    :goto_0
    const-string v1, "HelloThread"

    new-instance v2, Ljava/lang/StringBuilder;

    const-string v3, "Worker instance #"

    invoke-direct {v2, v3}, Ljava/lang/StringBuilder;-><init>(Ljava/lang/String;)V

    iget v3, p0, Lmy/example/HelloWorld/MainActivity$HelloThread;->id:I

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(I)Ljava/lang/StringBuilder;

    move-result-object v2

    const-string v3, " is running on thread "

    invoke-virtual {v2, v3}, Ljava/lang/StringBuilder;->append(Ljava/lang/String;)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-static {}, Ljava/lang/Thread;->currentThread()Ljava/lang/Thread;

    move-result-object v3

    invoke-virtual {v3}, Ljava/lang/Thread;->getId()J

    move-result-wide v3

    invoke-virtual {v2, v3, v4}, Ljava/lang/StringBuilder;->append(J)Ljava/lang/StringBuilder;

    move-result-object v2

    invoke-virtual {v2}, Ljava/lang/StringBuilder;->toString()Ljava/lang/String;

    move-result-object v2

    invoke-static {v1, v2}, Landroid/util/Log;->i(Ljava/lang/String;Ljava/lang/String;)I

    .line 143
    return-void

    .line 137
    :catch_0
    move-exception v0

    .line 138
    .local v0, e:Ljava/lang/InterruptedException;
    invoke-virtual {v0}, Ljava/lang/InterruptedException;->printStackTrace()V

    goto :goto_0
.end method
