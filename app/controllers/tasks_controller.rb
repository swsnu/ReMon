class TasksController < ApplicationController
  def show
    data = {:tasks => [{:id => "task_ev_0"}, {:id => "task_ev_1"}, {:id => "task_ev_2"}, {:id => "task_ev_3"}, {:id => "task_ev_4"}, ]}
    render :json => data
  end

  def status
    data = {:task_id => params[:id], :status => "RUNNING"}
    render :json => data
  end

  def logs
    data = {:task_id => params[:id], :logs => [
        {:time => 10000, :source => "ev_0", :level => "DEBUG", :message => "Hello, world!", :tags => [{:tag_name => "hello"}, {:tag_name => "test"}]},
        {:time => 20000, :source => "ev_0", :level => "DEBUG", :message => "Nice to meet you.", :tags => [{:tag_name => "test"}]},
    ]}
    render :json => data
  end
end
