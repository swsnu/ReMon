class TasksController < ApplicationController
  def show
    data = {:tasks => [{:id => "task_ev_0"}, {:id => "task_ev_1"}, {:id => "task_ev_2"}, {:id => "task_ev_3"}, {:id => "task_ev_4"}, ]}
    render :json => data
  end

  def status
    data = {:task_id => params[:id], :status => "RUNNING"}
    render :json => data
  end
end
