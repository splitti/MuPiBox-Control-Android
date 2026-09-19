#!/usr/bin/env python3
from __future__ import annotations
import argparse, json, threading
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer
from urllib.parse import urlparse

LOCK=threading.Lock()
STATE={
 "player":{"state":"paused","backend":"simulated","folder_id":"mock-folder","folder":"Mock Hörspiel","cover":"","queue":[{"id":"track-1","title":"Kapitel 1","provider":"local","resume_policy":"position"},{"id":"track-2","title":"Kapitel 2","provider":"local","resume_policy":"position"}],"index":0,"position":42.0,"duration":600.0,"volume":25,"max_volume":50},
 "spotify":{"connected":True,"playing":False,"paused":False,"buffering":False,"volume":32768,"volume_steps":65535,"track":None},
 "system":{"online":True,"wifi":{"connected":True,"interface":"wlan0","signal_dbm":-53,"quality_percent":78},"battery":{"available":True,"percent":73,"charging":True}}
}

def read_body(h):
    n=int(h.headers.get("Content-Length","0") or 0)
    return json.loads(h.rfile.read(n).decode()) if n else {}

class Handler(BaseHTTPRequestHandler):
    server_version="MuPiBoxMock/0.1"
    def log_message(self,fmt,*args): print("[mock] "+fmt%args)
    def send_json(self,code,payload):
        raw=json.dumps(payload).encode();self.send_response(code);self.send_header("Content-Type","application/json");self.send_header("Cache-Control","no-store");self.send_header("Content-Length",str(len(raw)));self.end_headers();self.wfile.write(raw)
    def do_GET(self):
        path=urlparse(self.path).path
        with LOCK:
            if path=="/api/health": return self.send_json(200,{"status":"ok","version":"mock-0.1.0"})
            if path=="/api/status": return self.send_json(200,STATE["player"])
            if path=="/api/system": return self.send_json(200,STATE["system"])
            if path=="/api/info": return self.send_json(200,{"version":"mock-0.1.0","simulation":True,"backend":"simulated","theme":"modern-dark","settings_persistent":True})
            if path=="/api/spotify/status": return self.send_json(200,STATE["spotify"])
            if path=="/api/connectivity/bluetooth": return self.send_json(200,{"enabled":True,"devices":[{"address":"AA:BB:CC:DD:EE:01","name":"Mock Speaker","paired":True,"trusted":True,"connected":True},{"address":"AA:BB:CC:DD:EE:02","name":"Mock Headphones","paired":True,"trusted":True,"connected":False}]})
        self.send_json(404,{"error":"not found"})
    def do_POST(self):
        path=urlparse(self.path).path
        try: payload=read_body(self)
        except Exception: return self.send_json(400,{"error":"invalid JSON"})
        if path=="/api/speak":
            text=str(payload.get("text","")).strip()
            if not text:return self.send_json(400,{"error":"text is required"})
            print("[mock] TTS request accepted");return self.send_json(200,{"ok":True})
        with LOCK:
            if path=="/api/command": return self.player_command(payload)
            if path=="/api/spotify/command": self.spotify_command(payload);return self.send_json(200,STATE["spotify"])
        self.send_json(404,{"error":"not found"})
    def player_command(self,payload):
        a=payload.get("action");p=STATE["player"]
        if a in ("play","resume"):p["state"]="playing"
        elif a=="pause":p["state"]="paused"
        elif a=="toggle":p["state"]="paused" if p["state"]=="playing" else "playing"
        elif a=="next":p["index"]=min(len(p["queue"])-1,p["index"]+1)
        elif a=="previous":p["index"]=max(0,p["index"]-1)
        elif a=="volume":p["volume"]=max(0,min(p["max_volume"],int(float(payload.get("value",0)))))
        elif a=="seek":p["position"]=max(0.0,min(p["duration"],float(payload.get("value",0))))
        elif a=="stop":p["state"]="stopped";p["position"]=0.0
        else:return self.send_json(400,{"error":f"unknown action {a!r}"})
        return self.send_json(200,p)
    def spotify_command(self,payload):
        a=payload.get("action");s=STATE["spotify"]
        if a in ("play","resume"):s["playing"],s["paused"]=True,False
        elif a=="pause":s["playing"],s["paused"]=False,True
        elif a=="volume":s["volume"]=max(0,min(s["volume_steps"],int(payload.get("value",0))))

def main():
    ap=argparse.ArgumentParser();ap.add_argument("--host",default="0.0.0.0");ap.add_argument("--port",type=int,default=8090);args=ap.parse_args();srv=ThreadingHTTPServer((args.host,args.port),Handler);print(f"MuPiBox mock listening on http://{args.host}:{args.port}");srv.serve_forever()
if __name__=="__main__":main()
