package com.auctionapp.auctionappjava.client.network;

import com.auctionapp.auctionappjava.client.config.ClientConfig;
import com.auctionapp.auctionappjava.common.dto.*;
import com.auctionapp.auctionappjava.common.enums.MessageKind;
import com.auctionapp.auctionappjava.common.util.JacksonSupport;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

public class SocketClient implements Closeable {
    private final ClientConfig config;
    private final ConcurrentMap<java.util.UUID, CompletableFuture<ApiEnvelope<?>>> pending = new ConcurrentHashMap<>();
    private final List<Consumer<AuctionEventDto>> eventListeners = new CopyOnWriteArrayList<>();
    private Socket socket; private BufferedReader in; private BufferedWriter out; private ExecutorService readerExecutor; private volatile boolean running;
    public SocketClient(ClientConfig config){ this.config=config; }
    public synchronized void connect() throws IOException { if(isConnected()) return; socket=new Socket(config.getHost(), config.getPort()); in=new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8)); out=new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8)); running=true; readerExecutor=Executors.newSingleThreadExecutor(r -> { Thread t=new Thread(r,"socket-client-reader"); t.setDaemon(true); return t; }); readerExecutor.submit(this::readLoop); }
    public boolean isConnected(){ return socket!=null && socket.isConnected() && !socket.isClosed(); }
    public ApiEnvelope<?> request(ApiEnvelope<? extends Serializable> request) { try { connect(); CompletableFuture<ApiEnvelope<?>> future=new CompletableFuture<>(); pending.put(request.getCorrelationId(), future); synchronized(this){ out.write(JacksonSupport.toJson(request)); out.newLine(); out.flush(); } return future.get(config.getTimeoutMillis(), TimeUnit.MILLISECONDS); } catch(Exception e){ throw new IllegalStateException("Không thể gọi server: " + e.getMessage(), e); } }
    private void readLoop(){ try{ String line; while(running && (line=in.readLine())!=null){ ApiEnvelope<Serializable> env=JacksonSupport.envelopeFromJson(line); if(env.getKind()== MessageKind.EVENT && env.getPayload() instanceof AuctionEventDto event){ for(Consumer<AuctionEventDto> l:eventListeners) l.accept(event); } else { CompletableFuture<ApiEnvelope<?>> f=pending.remove(env.getCorrelationId()); if(f!=null) f.complete(env); } } } catch(Exception e){ for(CompletableFuture<ApiEnvelope<?>> f: pending.values()) f.completeExceptionally(e); pending.clear(); } finally { close(); } }
    public void addEventListener(Consumer<AuctionEventDto> listener){ eventListeners.add(listener); }
    public void removeEventListener(Consumer<AuctionEventDto> listener){ eventListeners.remove(listener); }
    @Override public synchronized void close(){ running=false; try{ if(socket!=null) socket.close(); } catch(IOException ignored){} if(readerExecutor!=null) readerExecutor.shutdownNow(); }
}
